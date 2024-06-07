package experiments.aichat.config

import experiments.aichat.service.chat.function.file.FileRequest
import experiments.aichat.service.chat.function.file.FileResponse
import experiments.aichat.service.chat.function.GetOpenAPIFunction
import experiments.aichat.service.chat.function.file.FileFunction
import experiments.aichat.service.loader.FileProviderService
import org.springframework.ai.model.function.FunctionCallbackWrapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FunctionConfig(
    private val codeConfiguration: CodeConfiguration
) {

    @Bean
    fun getFileFunction(fileProviderService: FileProviderService): FunctionCallbackWrapper<FileRequest, FileResponse> =
        FunctionCallbackWrapper.builder(FileFunction(fileProviderService, codeConfiguration))
            .withName("getFileContent")
            .withDescription("Retrieve file content from the project, by file name")
            .build()

    @Bean
    fun getOpenApiDescription(): FunctionCallbackWrapper<FileRequest, FileResponse> =
        FunctionCallbackWrapper.builder(GetOpenAPIFunction(codeConfiguration))
            .withName("getOpenApi")
            .withDescription("Retrieve OpenAPI/Swagger specification for the project")
            .build()
}