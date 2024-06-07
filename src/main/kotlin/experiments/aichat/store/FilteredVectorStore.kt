package experiments.aichat.store

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.vectorstore.SimpleVectorStore

/**
 * simple vector store, but with simple filtering of metadata
 */
class FilteredVectorStore(embeddingClient: EmbeddingModel?) : SimpleVectorStore(embeddingClient) {

    fun filterMetadata(field:String, value: Any):String? =
        store.asSequence().filter {
            it.value.metadata[field] == value
        }.map {
            it.key
        }.toList().firstOrNull()
}
