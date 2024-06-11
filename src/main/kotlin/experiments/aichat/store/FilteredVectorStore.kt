package experiments.aichat.store

import experiments.aichat.service.loader.DocumentPipeline
import experiments.aichat.service.loader.DocumentPipeline.Companion
import experiments.aichat.service.loader.DocumentPipeline.Companion.logger
import org.apache.logging.log4j.kotlin.Logging
import org.springframework.ai.document.Document
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.core.io.Resource
import java.io.File
import java.util.*

/**
 * simple vector store, but with simple filtering of metadata
 */
class FilteredVectorStore(
    embeddingClient: EmbeddingModel?,
    private val cachePath: String,
    private val flushThreshold:Int = 10,
) : SimpleVectorStore(embeddingClient) {

    companion object: Logging

    private var addedDocuments:Int = 0

    fun filterMetadata(field:String, value: Any):String? =
        store.asSequence().filter {
            it.value.metadata[field] == value
        }.map {
            it.key
        }.toList().firstOrNull()

    override fun load(file: File?) {
        super.load(file)
        addedDocuments = 0
    }

    override fun load(resource: Resource?) {
        super.load(resource)
        addedDocuments = 0
    }

    fun load(): Date? {
        val file = File(cachePath)
        if (file.exists() && file.isFile) {
            load(file)

            val date = Date(file.lastModified())
            logger.info { "Vector DB loaded from file: $cachePath, last modified: $date" }
            return date
        }
        return null
    }

    override fun add(documents: MutableList<Document>?) {
        super.add(documents)
        addedDocuments ++

        if (addedDocuments % flushThreshold == 0) {
            flushToFile()
        }
    }

    fun flushToFile() {
        save(File(cachePath))
        logger.info { "Vector DB saved to file: $cachePath" }
    }
}
