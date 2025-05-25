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
    public void setLlm(Llm llm) { this.llm = llm; }

    public Inference getInference() { return inference; }
    public void setInference(Inference inference) { this.inference = inference; }

    public Prompt getPrompt() { return prompt; }
    public void setPrompt(Prompt prompt) { this.prompt = prompt; }

    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    public static class Llm {
        private String provider;
        private String prompt;
        private Ollama ollama;
        private Llama llama;

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }

        public Ollama getOllama() { return ollama; }
        public void setOllama(Ollama ollama) { this.ollama = ollama; }

        public Llama getLlama() { return llama; }
        public void setLlama(Llama llama) { this.llama = llama; }

        public static class Ollama {
            private String baseUrl;
            private String model;
            private double temperature;
            private int timeoutSec;

            public String getBaseUrl() { return baseUrl; }
            public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

            public String getModel() { return model; }
            public void setModel(String model) { this.model = model; }

            public double getTemperature() { return temperature; }
            public void setTemperature(double temperature) { this.temperature = temperature; }

            public int getTimeoutSec() { return timeoutSec; }
            public void setTimeoutSec(int timeoutSec) { this.timeoutSec = timeoutSec; }
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
            public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

            public String getApiKey() { return apiKey; }
            public void setApiKey(String apiKey) { this.apiKey = apiKey; }

            public String getModel() { return model; }
            public void setModel(String model) { this.model = model; }

            public double getTemperature() { return temperature; }
            public void setTemperature(double temperature) { this.temperature = temperature; }

            public int getMaxTokens() { return maxTokens; }
            public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

            public double getTopP() { return topP; }
            public void setTopP(double topP) { this.topP = topP; }

            public int getTopK() { return topK; }
            public void setTopK(int topK) { this.topK = topK; }
        }
    }

    public static class Inference {
        private int maxResults;
        private double minScore;

        public int getMaxResults() { return maxResults; }
        public void setMaxResults(int maxResults) { this.maxResults = maxResults; }

        public double getMinScore() { return minScore; }
        public void setMinScore(double minScore) { this.minScore = minScore; }
    }

    public static class Prompt {
        private String base;

        public String getBase() { return base; }
        public void setBase(String base) { this.base = base; }
    }

    public static class Metadata {
        private List<String> enrichWith;

        public List<String> getEnrichWith() { return enrichWith; }
        public void setEnrichWith(List<String> enrichWith) { this.enrichWith = enrichWith; }
    }
}