package experiments.aichat.service.loader

import org.apache.logging.log4j.kotlin.Logging
import org.springframework.stereotype.Service
import java.io.File

@Service
class FileProviderService {

    companion object : Logging

    private val fileExtensions = setOf("kt", "java", "gradle", "xml", "yaml", "properties", "md", "yml")

    fun loadFiles(path: String, callback: (File) -> Unit) {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            logger.error { "The specified path ($path) is not a valid directory." }
            return
        }
        directory.walk().forEach { file ->
            if (file.isFile && file.extension in fileExtensions) {
                val content = file.readText(Charsets.UTF_8)
                if (!isBinaryContent(content) && !file.shouldIgnore(content)) {
                    try {
                        callback(file)
                    } catch (e: Exception) {
                        logger.error(e) { "Error while handling $file: ${e.message}" }
                    }
                }
            }
        }
    }

    /**
     * Simple check to determine if the content is likely binary.
     * This function checks for the presence of null characters as a heuristic.
     */
    private fun isBinaryContent(content: String): Boolean = content.contains('\u0000')

    private fun File.shouldIgnore(content: String) =
        content.isBlank() || with(absolutePath) {
            contains("node_modules") || contains("/build/") || contains("/target/") || contains(".idea")
                    || contains("/integration/") || contains("/test/") || contains("/contract/")
        } || with(name) {
            startsWith(".") || endsWith(".class") || contains("Test")
        }
}
