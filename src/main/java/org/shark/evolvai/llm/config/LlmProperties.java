package org.shark.evolvai.llm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {
    private String provider;
    private String defaultPrompt;
    private OllamaProperties ollama;

    @Data
    public static class OllamaProperties {
        private String model;
        private String baseUrl;
    }
}
