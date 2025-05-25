package org.shark.evolvai.llm.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.shark.evolvai.config.RagProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LlmConfig {

    private final RagProperties ragProperties;

    public LlmConfig(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        RagProperties.Llm llm = ragProperties.getLlm();
        String provider = llm.getProvider();

        if ("llama".equalsIgnoreCase(provider)) {
            RagProperties.Llm.Llama llama = llm.getLlama();
            return OpenAiChatModel.builder()
                    .baseUrl(llama.getBaseUrl())
                    .apiKey(llama.getApiKey())
                    .modelName(llama.getModel())
                    .temperature(llama.getTemperature())
                    .maxTokens(llama.getMaxTokens())
                    .topP(llama.getTopP())
                    .build();
        } else if ("ollama".equalsIgnoreCase(provider)) {
            RagProperties.Llm.Ollama ollama = llm.getOllama();
            return OllamaChatModel.builder()
                    .baseUrl(ollama.getBaseUrl())
                    .modelName(ollama.getModel())
                    .temperature(ollama.getTemperature())
                    .timeout(Duration.ofSeconds(ollama.getTimeoutSec()))
                    .build();
        } else {
            throw new IllegalArgumentException("Proveedor LLM no válido: " + provider);
        }
    }
}