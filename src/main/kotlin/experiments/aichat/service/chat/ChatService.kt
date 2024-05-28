package experiments.aichat.service.chat

import experiments.aichat.config.CodeConfiguration
import org.springframework.ai.chat.ChatClient
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.io.File

@Service
class ChatService(
    val client: ChatClient,
    val vectorStore: VectorStore,
    val config: CodeConfiguration
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
                .createMessage(mapOf(
                    "message" to message,
                    "documents" to docs.toFiles()
                ))
            chatHistory.add(userMessage)
        }

        val messages = chatHistory
        return ChatResponse(
            responseText = client.call(Prompt(messages)).result.output.content,
            files = docs.getNames()
        )
    }

    fun clearChat() {
        chatHistory.clear()
    }

    private fun List<Document>.toFiles(): String =
        mapTo(mutableSetOf()) {
            val path = it.metadata["path"] as String
            File("${config.path}/$path")
        }.filter {
            it.exists()
        }.joinToString("\n") {
            val text = it.readText()
            """    
                ```
                $text
                ```
            """.trimIndent()
        }

    private fun List<Document>.getNames():Set<String> =
        mapTo(mutableSetOf()) {
            it.metadata["source"] as String
        }
}
