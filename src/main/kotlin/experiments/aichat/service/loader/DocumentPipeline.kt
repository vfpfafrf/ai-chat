package experiments.aichat.service.loader

import experiments.aichat.config.CodeConfiguration
import experiments.aichat.store.FilteredVectorStore
import org.apache.logging.log4j.kotlin.Logging
import org.springframework.ai.chat.ChatClient
import org.springframework.ai.document.Document
import org.springframework.ai.document.DocumentTransformer
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
    val client: ChatClient,
    val vectorStore: FilteredVectorStore,
    val config: CodeConfiguration
) {

    companion object : Logging

    fun loadCache(): Date? {
        with(getCacheFile()) {
            if (this.exists() && this.isFile) {
                vectorStore.load(this)
                val date = Date(this.lastModified())
                logger.info { "Vector DB loaded from file: ${this.absolutePath}, last modified: $date" }
                return date
            }
        }
        return Date()
    }

    fun loadFiles(lastModified: Date?) {
        with(config) {
            logger.info { "Start loading files from: $path" }
            populate(lastModified)
            saveToFile()
        }
    }

    private fun CodeConfiguration.populate(lastModified: Date?) {
        val basePath = File(path)
        val summaryMetadataEnricher = SummaryMetadataEnricher(client, listOf(CURRENT))
        val keywordMetadataEnricher = KeywordMetadataEnricher(client, 5)
        val splitter = TokenTextSplitter()
        var numOfFiles = 0

        loader.loadFiles(path) { file ->
            if (file.lastModified() < (lastModified?.time ?: -1)) {
                return@loadFiles
            }

            val path = file.relativeTo(basePath).toString()
            val id = vectorStore.filterMetadata("path", path)
            if (id != null) {
                vectorStore.delete(listOf(id))
            }

            val metadata = mapOf("path" to path)

            TikaDocumentReader(FileSystemResource(file)).get()
                .map { Document(it.content, it.metadata + metadata) }
                .enrichIf(enrichSummary, summaryMetadataEnricher)
                .enrichIf(enrichKeywords, keywordMetadataEnricher)
                .let {
                    splitter.apply(it).also { docs ->
                        vectorStore.add(docs)
                    }
                }

            numOfFiles++

            if (numOfFiles % 50 == 0) {
                saveToFile()
            }
        }
    }

    private fun List<Document>.enrichIf(condition: Boolean, enricher: DocumentTransformer): List<Document> =
        if (condition) enricher.apply(this) else this

    private fun saveToFile() {
        with(getCacheFile()) {
            vectorStore.save(this)
            logger.info { "Vector DB saved to file: ${this.absolutePath}" }
        }
    }

    private fun getCacheFile() = File("${config.project}.cache")
}
