package experiments.aichat.service.file

import experiments.aichat.config.CodeConfiguration
import experiments.aichat.service.loader.FileProviderService
import org.springframework.stereotype.Service
import java.io.File

@Service
class FileService(
    private val fileProviderService: FileProviderService,
    private val codeConfiguration: CodeConfiguration
) {
    fun resolveFile(fileName: String): File? =
        fileProviderService.getFileByName(fileName)
            .orElse {
                fileProviderService.getFileByName("$fileName.kt")
            }
            .orElse {
                getFile(fileName)
            }.orElse {
                getFile("${fileName}.kt")
            }.checkPath()

    private fun getFile(fileName: String): File? {
        val f = File("${codeConfiguration.path}${fileName}")
        return if (f.exists() && f.isFile) { f } else { null }
    }

    private fun File?.checkPath(): File? {
        if (this == null) {
            return null
        }
        val canonicalFile = this.canonicalFile
        val canonicalBase = File(codeConfiguration.path).canonicalFile
        return if (canonicalFile.toPath().startsWith(canonicalBase.toPath())) this else null
    }

    private fun File?.orElse(f: () -> File?): File? = this ?: f()
}
