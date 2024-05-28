package experiments.aichat

import experiments.aichat.console.Loader
import experiments.aichat.service.chat.ChatService
import experiments.aichat.service.loader.DocumentPipeline
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.*

@Component
class ConsoleChat(
    val loadPipeline: DocumentPipeline,
    val chatService: ChatService
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val loader = Loader()
        val lastModified:Date = loader.withLoader {
            val lastModified = loadPipeline.loadCache()
            loadPipeline.loadFiles(lastModified)
            lastModified
        } as Date

        val scanner = Scanner(System.`in`)
        println("Enter your messages (type '/exit' to quit, '+message' to continue with context):")

        while (true) {
            val input = scanner.nextLine()

            if (input.isBlank()) continue
            if (input.equals("/exit", ignoreCase = true)) break
            if (!input.startsWith("+")) {
                chatService.clearChat()
            }
            if (input.equals("/update", ignoreCase = true)) {
                loader.withLoader {
                    loadPipeline.loadFiles(lastModified)
                }
                continue
            }

            val response = chatService.ask(input)
            println(response.responseText)
            println("More info: ${response.files}")
        }
    }
}
