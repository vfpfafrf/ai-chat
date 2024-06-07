package experiments.aichat.service.chat.function.file

import com.fasterxml.jackson.annotation.JsonProperty

data class FileRequest (
    @JsonProperty("fileName")
    val fileName: String
)
