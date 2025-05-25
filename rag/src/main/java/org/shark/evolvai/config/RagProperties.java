// rag/src/main/java/org/shark/evolvai/config/RagProperties.java
package org.shark.evolvai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private Llm llm;
    private Inference inference;
    private Prompt prompt;
    private Metadata metadata;

    public Llm getLlm() { return llm; }
    public Inference getInference() { return inference; }
    public Prompt getPrompt() { return prompt; }
    public Metadata getMetadata() { return metadata; }

    public static class Llm {
        private String provider;
        private String prompt;
        private Ollama ollama;
        private Llama llama;

        public String getProvider() { return provider; }
        public String getPrompt() { return prompt; }
        public Ollama getOllama() { return ollama; }
        public Llama getLlama() { return llama; }

        public static class Ollama {
            private String baseUrl;
            private String model;
            private double temperature;
            private int timeoutSec;

            public String getBaseUrl() { return baseUrl; }
            public String getModel() { return model; }
            public double getTemperature() { return temperature; }
            public int getTimeoutSec() { return timeoutSec; }
        }

        public static class Llama {
            private String baseUrl;
            private String apiKey;
            private String model;
            private double temperature;
            private int maxTokens;
            private double topP;
            private int topK;

            public String getBaseUrl() { return baseUrl; }
            public String getApiKey() { return apiKey; }
            public String getModel() { return model; }
            public double getTemperature() { return temperature; }
            public int getMaxTokens() { return maxTokens; }
            public double getTopP() { return topP; }
            public int getTopK() { return topK; }
        }
    }

    public static class Inference {
        private int maxResults;
        private double minScore;

        public int getMaxResults() { return maxResults; }
        public double getMinScore() { return minScore; }
    }

    public static class Prompt {
        private String base;

        public String getBase() { return base; }
    }

    public static class Metadata {
        private List<String> enrichWith;

        public List<String> getEnrichWith() { return enrichWith; }
    }
}