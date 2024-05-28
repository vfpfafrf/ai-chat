package experiments.aichat.service.chat

data class ChatResponse(
    val responseText: String,
    val files: Set<String>
)
