package org.shark.evolvai.llm.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LlmConfig {

    @Value("${llm.provider}")
    private String provider;

    @Bean
    public ChatLanguageModel chatLanguageModel(
            // OpenAI/Llama params (solo si se usa)
            @Value("${llm.llama.base-url:}") String llamaBaseUrl,
            @Value("${llm.llama.api-key:}") String llamaApiKey,
            @Value("${llm.llama.model:}") String llamaModel,
            @Value("${llm.llama.temperature:0.7}") double llamaTemperature,
            @Value("${llm.llama.max-tokens:512}") int llamaMaxTokens,
            @Value("${llm.llama.top-p:0.95}") double llamaTopP,
            @Value("${llm.llama.top-k:50}") int llamaTopK,

            // Ollama params (solo si se usa)
            @Value("${llm.ollama.base-url:}") String ollamaBaseUrl,
            @Value("${llm.ollama.model:}") String ollamaModel,
            @Value("${llm.ollama.temperature:0.7}") double ollamaTemperature,
            @Value("${llm.ollama.timeout-sec:180}") int ollamaTimeoutSec
    ) {
        if ("llama".equalsIgnoreCase(provider)) {
            return OpenAiChatModel.builder()
                    .baseUrl(llamaBaseUrl)
                    .apiKey(llamaApiKey)
                    .modelName(llamaModel)
                    .temperature(llamaTemperature)
                    .maxTokens(llamaMaxTokens)
                    .topP(llamaTopP)
                    .build();
        } else if ("ollama".equalsIgnoreCase(provider)) {
            return OllamaChatModel.builder()
                    .baseUrl(ollamaBaseUrl)
                    .modelName(ollamaModel)
                    .temperature(ollamaTemperature)
                    .timeout(Duration.ofSeconds(ollamaTimeoutSec))
                    .build();
        } else {
            throw new IllegalArgumentException("Proveedor LLM no válido: " + provider);
        }
    }
}
