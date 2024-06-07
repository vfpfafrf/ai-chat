package experiments.aichat.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig(
    @Value("\${code.summary}")
    val enrichSummary: Boolean,
    @Value("\${code.keywords}")
    val enrichKeywords: Boolean,
    @Value("\${code.path}")
    val path: String,
    @Value("\${code.openapi}")
    val openapi: String,
    @Value("\${code.name}")
    val project: String,
    @Value("\${code.tech}")
    val tech: String,
    @Value("\${code.answer}")
    val answerLang: String
) {
    @Bean
    fun pipelineConfiguration() =
        CodeConfiguration(
            enrichSummary = enrichSummary,
            enrichKeywords = enrichKeywords,
            path = if (path.endsWith("/")) path else "$path/",
            openapi = if (openapi.lowercase() == "none") null else openapi,
            project = project,
            tech = tech,
            answerLang = answerLang
        )
}