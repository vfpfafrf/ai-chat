# ai-code-chat

Implementation of Retrieval-Augmented Generation pipeline to build console chat application, to asking questions about code.

# Story with detailed explanation of how that works

Medium link: https://medium.com/picsart-engineering/utilise-rag-framework-to-become-a-10x-developer-19b7be05a29c

# Configuration example

While by default it configured to use local LLama LLM, better results are shown with OpenAI.

Configuration with OpenAI for project, located at `/Users/user/IdeaProjects/project` path

```yaml
code:
  summary: true
  keywords: true
  path: /Users/user/IdeaProjects/project
  openapi: /Users/user/IdeaProjects/project/specs/openapi.yaml
  name: my-project-name

spring:
  ai:
    ollama:
      chat:
        enabled: false
      embedding:
        enabled: false
    openai:
      api-key: sk-your-open-ai-key
      chat:
        enabled: true
        options:
          model: gpt-4o
          temperature: 0.1
      embedding:
        enabled: true
        options:
          model: text-embedding-3-small
```

To run jar with given profile:

```bash
java -jar ai-chat.jar --spring.profiles.active=my-project -spring.config.location=application-my-project.yaml
````

