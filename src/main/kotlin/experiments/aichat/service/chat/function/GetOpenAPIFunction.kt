package experiments.aichat.service.chat.function

import experiments.aichat.config.CodeConfiguration
import experiments.aichat.service.chat.function.file.FileRequest
import experiments.aichat.service.chat.function.file.FileResponse
import java.io.File
import java.util.function.Function

class GetOpenAPIFunction(
    private val config: CodeConfiguration
) : Function<FileRequest, FileResponse> {

    override fun apply(request: FileRequest): FileResponse {
        val content = if (config.openapi == null) {
            "No OpenAPI specification configured for the project"
        } else {
            File(config.openapi).readText()
        }
        return FileResponse(content)
    }
}
