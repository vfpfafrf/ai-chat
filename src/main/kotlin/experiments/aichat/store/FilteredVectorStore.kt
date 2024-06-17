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
import java.util.concurrent.atomic.AtomicInteger

/**
 * simple vector store, but with simple filtering of metadata
 */
class FilteredVectorStore(
    embeddingClient: EmbeddingModel?,
    private val cachePath: String,
    private val flushThreshold:Int = 10,
) : SimpleVectorStore(embeddingClient) {

    companion object: Logging

    fun filterMetadata(field:String, value: Any):String? =
        store.asSequence().filter {
            it.value.metadata[field] == value
        }.map {
            it.key
        }.toList().firstOrNull()

    /**
     * load storage from cache
     * @return Date - last modified date of cache, or null
     */
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

        if (store.size % flushThreshold == 0) {
            flushToFile()
        }
    }

    fun flushToFile() {
        save(File(cachePath))
        logger.info { "Vector DB saved to file: $cachePath" }
    }
}
