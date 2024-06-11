package experiments.aichat.config

import experiments.aichat.store.FilteredVectorStore
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VectorStoreConfig(
    val config: CodeConfiguration,
    @Value("\${vectorStore.cache.flushThreshold}")
    val flushThreshold: Int = 10
) {

    @Bean
    fun vectorStore(embeddingClient: EmbeddingModel): FilteredVectorStore =
        FilteredVectorStore(
            embeddingClient = embeddingClient,
            flushThreshold = flushThreshold,
            cachePath = config.getCacheFileName()
        )
}