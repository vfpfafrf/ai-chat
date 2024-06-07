package experiments.aichat.service.chat.function.file

import experiments.aichat.service.loader.FileProviderService
import java.io.File
import java.util.function.Function

class FileFunction(
    private val fileProviderService: FileProviderService
) : Function<FileRequest, FileResponse> {

    override fun apply(request: FileRequest): FileResponse {
        val fileName = request.fileName
        return (loadFile(fileName) ?: loadFile("$fileName.kt")) ?: FileResponse("File not found in the project")
    }

    private fun loadFile(fileName: String): FileResponse? = fileProviderService.getFileByName(fileName)?.toResponse()

    private fun File.toResponse() = FileResponse(this.readText())
}
