package experiments.aichat.config

data class CodeConfiguration(
    val enrichSummary: Boolean,
    val enrichKeywords: Boolean,
    val path: String,
    val openapi: String?,
    val project: String,
    val tech: String,
    val answerLang: String
) {

    fun getCacheFileName() = "$project.cache"
}
