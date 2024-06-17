package experiments.aichat.service.transformer

data class KotlinDocument(
    val content: String,
    val linkedFiles: List<String>
)
