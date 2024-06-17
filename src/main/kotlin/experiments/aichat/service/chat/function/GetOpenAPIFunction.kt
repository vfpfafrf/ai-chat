package experiments.aichat.service.chat.function

import experiments.aichat.config.CodeConfiguration
import experiments.aichat.service.chat.function.file.FileRequest
import experiments.aichat.service.chat.function.file.FileResponse
import java.io.File
import java.util.function.Function

class GetOpenAPIFunction(
    private val path: String,
) : Function<FileRequest, FileResponse> {

    override fun apply(request: FileRequest): FileResponse {
        val file = File(path)
        val content = if(file.exists()) file.readText() else "No OpenAPI specification provided"
        return FileResponse(content)
    }
}
