// Archivo: RagConfigEndpoint.java
package org.shark.evolvai.monitoring;

import lombok.RequiredArgsConstructor;
import org.shark.evolvai.embedding.config.TokenizerConfig;
import org.shark.evolvai.embedding.domain.service.TextChunkingService;
import org.shark.evolvai.inference.config.InferenceProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Endpoint(id = "rag-config")
@RequiredArgsConstructor
public class RagConfigEndpoint {

    private final InferenceProperties inferenceProperties;
    private final TokenizerConfig tokenizerConfig;
    private final TextChunkingService chunkingService;

    // YAML-based values
    @Value("${embedding.generator.type:undefined}")
    private String embeddingGeneratorType;
    @Value("${embedding.storage.type:undefined}")
    private String embeddingStorageType;

    @Value("${embedding.pgvector.host:undefined}")
    private String pgHost;
    @Value("${embedding.pgvector.port:5432}")
    private int pgPort;
    @Value("${embedding.pgvector.database:undefined}")
    private String pgDatabase;
    @Value("${embedding.pgvector.user:undefined}")
    private String pgUser;
    @Value("${embedding.pgvector.tableName:undefined}")
    private String tableName;
    @Value("${embedding.pgvector.dimensions:0}")
    private int dimensions;

    @Value("${llm.default-prompt:undefined}")
    private String defaultPrompt;

    @Value("${llm.ollama.model:undefined}")
    private String ollamaModel;
    @Value("${llm.ollama.base-url:undefined}")
    private String ollamaUrl;
    @Value("${llm.ollama.temperature:0.0}")
    private double ollamaTemp;
    @Value("${llm.ollama.timeout-sec:0}")
    private int ollamaTimeout;

    @Value("${llm.llama.model:undefined}")
    private String llamaModel;
    @Value("${llm.llama.base-url:undefined}")
    private String llamaUrl;
    @Value("${llm.llama.api-key:undefined}")
    private String llamaApiKey;
    @Value("${llm.llama.temperature:0.0}")
    private double llamaTemp;
    @Value("${llm.llama.max-tokens:0}")
    private int llamaMaxTokens;

    @Value("${chatmemory-persistence.provider:undefined}")
    private String memoryProvider;
    @Value("${chatmemory-persistence.memory.file-path:undefined}")
    private String memoryFile;
    @Value("${chatmemory-persistence.jpa.table-name:undefined}")
    private String memoryTable;

    @Value("${spring.datasource.url:undefined}")
    private String dbUrl;
    @Value("${spring.datasource.username:undefined}")
    private String dbUser;
    @Value("${spring.jpa.hibernate.ddl-auto:undefined}")
    private String ddlAuto;

    @ReadOperation
    public Map<String, Object> config() {
        Map<String, Object> info = new LinkedHashMap<>();

        info.put("embedding.generator.type", embeddingGeneratorType);
        info.put("embedding.storage.type", embeddingStorageType);
        info.put("embedding.pgvector.host", pgHost);
        info.put("embedding.pgvector.port", pgPort);
        info.put("embedding.pgvector.database", pgDatabase);
        info.put("embedding.pgvector.user", pgUser);
        info.put("embedding.pgvector.tableName", tableName);
        info.put("embedding.pgvector.dimensions", dimensions);

        info.put("llm.defaultPrompt", defaultPrompt);

        info.put("llm.ollama.model", ollamaModel);
        info.put("llm.ollama.baseUrl", ollamaUrl);
        info.put("llm.ollama.temperature", ollamaTemp);
        info.put("llm.ollama.timeoutSec", ollamaTimeout);

        info.put("llm.llama.model", llamaModel);
        info.put("llm.llama.baseUrl", llamaUrl);
        info.put("llm.llama.apiKey", llamaApiKey);
        info.put("llm.llama.temperature", llamaTemp);
        info.put("llm.llama.maxTokens", llamaMaxTokens);

        info.put("chatmemory.provider", memoryProvider);
        info.put("chatmemory.memory.filePath", memoryFile);
        info.put("chatmemory.jpa.tableName", memoryTable);

        info.put("datasource.url", dbUrl);
        info.put("datasource.user", dbUser);
        info.put("jpa.ddl-auto", ddlAuto);

        info.put("tokenizer.path", tokenizerConfig.getLoadedPath());
        info.put("tokenizer.maxLength", tokenizerConfig.getMaxLength());

        info.put("chunking.chunkSize", chunkingService.getChunkSize());
        info.put("chunking.overlap", chunkingService.getOverlap());

        info.put("inference.maxResults", inferenceProperties.getMaxResults());
        info.put("inference.minScore", inferenceProperties.getMinScore());

        return info;
    }
}