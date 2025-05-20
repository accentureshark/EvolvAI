package org.shark.evolvai.llm.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlamaConfig {

    @Bean
    public OpenAiChatModel llamaModel(
            @Value("${llm.llama.base-url}") String baseUrl,
            @Value("${llm.llama.api-key:}") String apiKey,
            @Value("${llm.llama.model}") String model
    ) {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(model)
                .build();
    }
}