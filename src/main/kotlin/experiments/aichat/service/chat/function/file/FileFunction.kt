package experiments.aichat.service.chat.function.file

import experiments.aichat.config.CodeConfiguration
import experiments.aichat.service.file.FileService
import experiments.aichat.service.loader.FileProviderService
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.util.function.Function

class FileFunction(
    private val fileService: FileService,
) : Function<FileRequest, FileResponse> {

    companion object : Logging

    override fun apply(request: FileRequest): FileResponse {
        val fileName = request.fileName
        logger.debug { "Call GetFile function with fileName=$fileName" }
        return fileService.resolveFile(fileName)?.toResponse() ?: FileResponse("File not found in the project")
    }

    private fun File.toResponse() = FileResponse(this.readText())
}
