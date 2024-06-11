package experiments.aichat.service.chat.function.file

import experiments.aichat.config.CodeConfiguration
import experiments.aichat.service.loader.FileProviderService
import org.apache.logging.log4j.kotlin.Logging
import java.io.File
import java.util.function.Function

class FileFunction(
    private val fileProviderService: FileProviderService,
    private val codeConfiguration: CodeConfiguration
) : Function<FileRequest, FileResponse> {

    companion object : Logging

    override fun apply(request: FileRequest): FileResponse {
        val fileName = request.fileName
        logger.debug { "Call GetFile function with fileName=$fileName" }
        return resolveFile(fileName)?.toResponse() ?: FileResponse("File not found in the project")
    }

    private fun resolveFile(fileName: String): File? =
        fileProviderService.getFileByName(fileName)
            .orElse {
                fileProviderService.getFileByName("$fileName.kt")
            }
            .orElse {
                File("${codeConfiguration.path}${fileName}")
            }.orElse {
                File("${codeConfiguration.path}${fileName}.kt")
            }.checkPath()

    private fun File?.checkPath(): File? {
        if (this == null) {
            return null
        }
        val canonicalFile = this.canonicalFile
        val canonicalBase = File(codeConfiguration.path).canonicalFile
        return if (canonicalFile.toPath().startsWith(canonicalBase.toPath())) this else null
    }

    private fun File.toResponse() = FileResponse(this.readText())

    private fun File?.orElse(f: () -> File?): File? = this ?: f()
}
