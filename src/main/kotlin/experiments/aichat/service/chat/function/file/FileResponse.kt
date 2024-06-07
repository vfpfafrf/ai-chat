package experiments.aichat.service.chat.function.file

import com.fasterxml.jackson.annotation.JsonProperty

data class FileResponse(
    @JsonProperty("content")
    val content: String
)
