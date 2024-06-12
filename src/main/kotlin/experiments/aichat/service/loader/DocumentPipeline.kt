package experiments.aichat.service.loader

import experiments.aichat.config.CodeConfiguration
import experiments.aichat.store.FilteredVectorStore
import org.apache.logging.log4j.kotlin.Logging
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.document.Document
import org.springframework.ai.document.DocumentTransformer
import org.springframework.ai.reader.TextReader
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.ai.transformer.KeywordMetadataEnricher
import org.springframework.ai.transformer.SummaryMetadataEnricher
import org.springframework.ai.transformer.SummaryMetadataEnricher.SummaryType.CURRENT
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

@Component
class DocumentPipeline(
    val loader: FileProviderService,
    val client: ChatModel,
    val vectorStore: FilteredVectorStore,
    val config: CodeConfiguration
) {

    companion object : Logging

    suspend fun loadFiles(): Date =
        with(config) {
            val lastModified = vectorStore.load()
            logger.info { "Start loading files from: $path" }
            populate(lastModified)
            vectorStore.flushToFile()
            lastModified ?: Date()
        }

    suspend fun update(lastModified: Date) =
        with(config) {
            populate(lastModified)
            vectorStore.flushToFile()
        }

    private suspend fun CodeConfiguration.populate(lastModified: Date?) {
        val basePath = File(path)
        val summaryMetadataEnricher = SummaryMetadataEnricher(client, listOf(CURRENT))
        val keywordMetadataEnricher = KeywordMetadataEnricher(client, 5)
        val splitter = TokenTextSplitter()

        loader.loadFiles(path) { file ->
            val path = file.relativeTo(basePath).toString()
            val id = vectorStore.filterMetadata("path", path)

            if (id != null && file.lastModified() < (lastModified?.time ?: -1)) {
                return@loadFiles
            }

            processFile(
                file = file,
                id = id,
                summaryMetadataEnricher = summaryMetadataEnricher,
                keywordMetadataEnricher = keywordMetadataEnricher,
                splitter = splitter
            )
        }
    }

    private suspend fun CodeConfiguration.processFile(
        file: File,
        id: String?,
        summaryMetadataEnricher: SummaryMetadataEnricher,
        keywordMetadataEnricher: KeywordMetadataEnricher,
        splitter: TokenTextSplitter
    ) {
        if (id != null) {
            vectorStore.delete(listOf(id))
        }

        val basePath = File(path)
        val filePath = file.relativeTo(basePath).toString()
        val metadata = mapOf("path" to filePath)

        file.toDocumentsList()
            .map { Document(it.content, it.metadata + metadata) }
            .enrichIf(enrichSummary, summaryMetadataEnricher)
            .enrichIf(enrichKeywords, keywordMetadataEnricher)
            .let {
                splitter.apply(it).also { docs ->
                    vectorStore.add(docs)
                }
            }
    }

    private suspend fun File.toDocumentsList() =
        FileSystemResource(this).let {
            try {
                TikaDocumentReader(it).get()
            } catch (_: Throwable) {
                TextReader(it).get()
            }
        }

    private fun List<Document>.enrichIf(condition: Boolean, enricher: DocumentTransformer): List<Document> =
        if (condition) enricher.apply(this) else this
}
