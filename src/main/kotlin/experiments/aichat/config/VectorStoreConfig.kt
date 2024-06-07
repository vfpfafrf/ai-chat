package experiments.aichat.config

import experiments.aichat.store.FilteredVectorStore
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VectorStoreConfig {

    @Bean
    fun vectorStore(embeddingClient: EmbeddingModel): FilteredVectorStore = FilteredVectorStore(embeddingClient)
}