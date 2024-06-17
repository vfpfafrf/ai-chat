package experiments.aichat.service.transformer

import org.springframework.ai.document.Document
import org.treesitter.TSNode
import org.treesitter.TSParser
import org.treesitter.TreeSitterKotlin

class KotlinDocumentTransformer(
    document: Document
) {

    private val standardImports = setOf("java.", "kotlin.", "org.springframework.", "javax.", "org.apache.")

    private val text = document.content

    fun process(): KotlinDocument {
        val nonStandardImports = mutableListOf<String>()
        val comments = mutableListOf<String>()
        var packageHeader: String? = null
        var className: String? = null
        val functions = mutableListOf<String>()

        val parser = TSParser()
        parser.setLanguage(TreeSitterKotlin())
        val tree = parser.parseString(null, text)

        val node = tree.rootNode
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            when (child.type) {
                "package_header" -> packageHeader = child.nodeText()
                "multiline_comment" -> child.nodeText()?.let { comments.add(it) }
                "import_list" -> {
                    val allImports = child.collect("import_header", "identifier")
                    nonStandardImports += allImports.filterNot { all ->
                        standardImports.any { import -> all.startsWith(import) }
                    }
                    comments += child.collect("import_header", "multiline_comment")
                }

                "class_declaration" -> {
                    className = child.childByType("type_identifier").nodeText()

                    functions += child.forEach("class_body") {
                        it.collectFunctions()
                    } ?: emptyList()
                }
            }
        }
        val result = if (className == null || functions.isEmpty()) {
            text
        } else {
            """
            $packageHeader
            
            ${nonStandardImports.joinToString("\n") { "import $it" }}
            
            ${comments.joinToString("\n")}
            class $className {
            
                ${functions.joinToString("\n")}

            }
            """.trimIndent()
        }
        return KotlinDocument(
            content =  result,
            linkedFiles = nonStandardImports.toLinkedFiles(packageHeader)
        )
    }

    private fun TSNode.collectFunctions(): List<String> {
        val result = mutableListOf<String>()
        var comment = ""

        for (i in 0 until childCount) {
            val child = getChild(i)
            if (child.type == "multiline_comment" && child.nextSibling.type == "function_declaration") {
                child.nodeText()?.let { comment = it }
            }
            if (child.type == "function_declaration") {
                val functionName = child.childByType("simple_identifier").nodeText()
                val functionParams = child.childByType("function_value_parameters").nodeText()

                if (functionName?.isNotBlank() == true) {
                    if (comment.isBlank()) {
                        comment = "// ${functionName.camelCaseToWords()}"
                    }
                    result += "$comment\nfun $functionName $functionParams {}"
                    comment = ""
                }
            }
        }
        return result
    }

    private fun TSNode.collect(type: String, name: String): List<String> {
        val result = mutableListOf<String>()
        this.forEach(type, findFirst = false) {
            it.childByType(name).nodeText()?.let { text -> result.add(text) }
        }
        return result
    }

    private fun <T> TSNode.forEach(type: String, findFirst:Boolean = true, f: (TSNode) -> T): T? {
        for (i in 0 until childCount) {
            val child = getChild(i)
            if (child.type == type) {
                val result = f(child)
                if (findFirst) {
                    return result
                }
            }
        }
        return null
    }

    private fun TSNode.childByType(type: String): TSNode? = this.forEach(type) { it }

    private fun TSNode?.nodeText():String? =
        this?.let { text.substring(it.startByte, kotlin.math.min(it.endByte, text.length - 1)).trimIndent() }

    private fun List<String>.toLinkedFiles(packageHeader:String?):List<String> {
        val prefix = packageHeader?.replace("package ", "")?.split(".")?.take(3)?.joinToString(".") ?: ""
        return filter { it.startsWith(prefix) }
            .flatMap { it.split(".")
            .takeLast(2) }
            .distinct()
            .filter { it.isNotBlank() && it != "*" && !it.isUpperCase() && !it.isLowerCase() && !it[0].isLowerCase() }
    }

    private fun String.camelCaseToWords(): String = replace(Regex("([a-z])([A-Z])"), "$1 $2").lowercase()

    private fun String.isUpperCase(): Boolean = this.all { it.isUpperCase() || it == '_' }

    private fun String.isLowerCase(): Boolean = this.all { it.isLowerCase() }
}
