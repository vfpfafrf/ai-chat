package experiments.aichat.service.chat

import experiments.aichat.config.CodeConfiguration
import experiments.aichat.main
import experiments.aichat.service.file.FileService
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.File


@Service
class ChatService(
    val client: ChatModel,
    val vectorStore: VectorStore,
    val config: CodeConfiguration,
    val fileService: FileService
) {
    @Value("classpath:/prompts/system.txt")
    private val systemPrompt: Resource? = null

    @Value("classpath:/prompts/user.txt")
    private val userPrompt: Resource? = null

    @Value("classpath:/prompts/chat.txt")
    private val userInChatPrompt: Resource? = null

    private val chatHistory = mutableListOf<Message>()

    fun ask(message: String): ChatResponse {
        val request = SearchRequest.query(message).withTopK(5)
        val docs: List<Document> = vectorStore.similaritySearch(request)

        if (chatHistory.isEmpty()) {
            val systemPromptTemplate = SystemPromptTemplate(systemPrompt)
                .createMessage(
                    mapOf(
                        "documents" to docs.toFiles(),
                        "tech" to config.tech,
                        "project" to config.project,
                        "answerLang" to config.answerLang
                    )
                )
            val userMessage = PromptTemplate(userPrompt)
                .createMessage(mapOf("message" to message))

            chatHistory.add(systemPromptTemplate)
            chatHistory.add(userMessage)
        } else {
            val userMessage = PromptTemplate(userInChatPrompt)
                .createMessage(
                    mapOf(
                        "message" to message,
                        "documents" to docs.toFiles()
                    )
                )
            chatHistory.add(userMessage)
        }

        val messages = chatHistory
        val prompt = if (client is OpenAiChatModel) {
            val builder = OpenAiChatOptions.builder().withFunction("getFileContent")
            if (config.openapi != null) {
                builder.withFunction("getOpenApi")
            }
            Prompt(messages, builder.build())
        } else {
            Prompt(messages)
        }

        return ChatResponse(
            responseText = client.call(prompt).result.output.content,
            files = docs.getNames()
        )
    }

    fun clearChat() {
        chatHistory.clear()
    }

    private fun List<Document>.toFiles(): String {
        val mainFiles = mapTo(mutableSetOf()) {
            val path = it.metadata["path"] as String
            File("${config.path}/$path")
        }.filter {
            it.exists()
        }
        val linkedFiles = linkedFiles()

        return (mainFiles + linkedFiles.take(4)).distinct().joinToString("\n") {
            val text = it.readText()
            """    
                ```
                $text
                ```
            """.trimIndent()
        }
    }

    private fun List<Document>.linkedFiles():List<File> =
        mapNotNull{ it.metadata["linked"] as List<String>? }
            .flatMap {
                it.map { fileName ->
                    fileService.resolveFile(fileName)
                }
            }.filterNotNull()

    private fun List<Document>.getNames(): Set<String> =
        mapTo(mutableSetOf()) {
            it.metadata["source"] as String
        }
}
