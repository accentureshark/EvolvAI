package org.shark.evolvai.monitoring;

import lombok.RequiredArgsConstructor;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.config.TokenizerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Endpoint(id = "rag-config")
@RequiredArgsConstructor
public class RagConfigEndpoint {

    private final RagProperties ragProperties;
    private final TokenizerConfig tokenizerConfig;
    //private final TextChunkingService chunkingService;

    @Value("${embedding.provider:undefined}")
    private String embeddingProvider;

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

    @Value("${llm.ollama.model:undefined}")
    private String ollamaModel;
    @Value("${llm.ollama.base-url:undefined}")
    private String ollamaUrl;

    @Value("${rag.prompt.base:undefined}")
    private String basePrompt;

    @Value("#{'${rag.metadata.enrich-with:}'.split(',')}")
    private List<String> enrichKeys;

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

        info.put("embedding.provider", embeddingProvider);
        info.put("embedding.pgvector.host", pgHost);
        info.put("embedding.pgvector.port", pgPort);
        info.put("embedding.pgvector.database", pgDatabase);
        info.put("embedding.pgvector.user", pgUser);
        info.put("embedding.pgvector.tableName", tableName);
        info.put("embedding.pgvector.dimensions", dimensions);

        info.put("llm.ollama.model", ollamaModel);
        info.put("llm.ollama.baseUrl", ollamaUrl);

        info.put("rag.prompt.base", basePrompt);
        info.put("rag.metadata.enrichKeys", enrichKeys);
        //info.put("chunking.chunkSize", chunkingService.getChunkSize());
        //info.put("chunking.overlap", chunkingService.getOverlap());

        //info.put("tokenizer.path", tokenizerConfig.getLoadedPath());
        //info.put("tokenizer.maxLength", tokenizerConfig.getMaxLength());

        //info.put("inference.maxResults", ragProperties.getInference().getMaxResults());
        //info.put("inference.minScore", ragProperties.getInference().getMinScore());

        info.put("chatmemory.provider", memoryProvider);
        info.put("chatmemory.memory.filePath", memoryFile);
        info.put("chatmemory.jpa.tableName", memoryTable);

        info.put("datasource.url", dbUrl);
        info.put("datasource.user", dbUser);
        info.put("jpa.ddl-auto", ddlAuto);

        return info;
    }
}
