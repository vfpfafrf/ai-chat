package experiments.aichat.service.transformer

import org.springframework.ai.document.Document
import org.springframework.ai.document.DocumentTransformer

class KotlinTransformer : DocumentTransformer {
    override fun apply(documents: List<Document>?): List<Document> =
        documents?.map { doc ->
            KotlinDocumentTransformer(doc).process().let {
                val metadata = mapOf("linked" to it.linkedFiles) + doc.metadata

                Document(doc.id, it.content, doc.media.toList(), metadata).also {
                    doc.contentFormatter = doc.contentFormatter
                }
            }
        } ?: emptyList()
}
