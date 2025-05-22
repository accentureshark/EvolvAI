package org.shark.evolvai.monitoring;

import org.shark.evolvai.embedding.domain.service.TextChunkingService;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.shark.evolvai.embedding.config.TokenizerConfig;
import org.shark.evolvai.llm.port.out.LlmProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Endpoint(id = "rag")
public class RagActuatorEndpoint {

    private final EmbeddingGenerator embeddingGenerator;
    private final EmbeddingStorage embeddingStorage;
    private final LlmProvider llmProvider;
    private final TokenizerConfig tokenizerConfig;
    private final TextChunkingService chunkingService;

    public RagActuatorEndpoint(
            EmbeddingGenerator embeddingGenerator,
            EmbeddingStorage embeddingStorage,
            LlmProvider llmProvider,
            TokenizerConfig tokenizerConfig,
            TextChunkingService chunkingService
    ) {
        this.embeddingGenerator = embeddingGenerator;
        this.embeddingStorage = embeddingStorage;
        this.llmProvider = llmProvider;
        this.tokenizerConfig = tokenizerConfig;
        this.chunkingService = chunkingService;
    }

    @Value("${llm.ollama.model:undefined}")
    private String llmModel;

    @Value("${llm.ollama.base-url:undefined}")
    private String llmBaseUrl;

    @Value("${llm.default-prompt:undefined}")
    private String defaultPrompt;

    @Value("${embedding.pgvector.host:undefined}")
    private String pgHost;

    @Value("${embedding.pgvector.tableName:undefined}")
    private String tableName;

    @Value("${embedding.pgvector.dimensions:0}")
    private int dimensions;

    @Value("${inference.maxResults:5}")
    private int maxResults;

    @Value("${inference.minScore:0.7}")
    private double minScore;

    @Value("${chunking.chunkSize:500}")
    private int chunkSize;

    @Value("${chunking.overlap:100}")
    private int chunkOverlap;

    @ReadOperation
    public Map<String, Object> ragInfo() {
        Map<String, Object> info = new LinkedHashMap<>();

        // LLM
        info.put("llm.model", llmModel);
        info.put("llm.baseUrl", llmBaseUrl);
        info.put("llm.defaultPrompt", defaultPrompt);
        info.put("llm.bean", llmProvider.getClass().getName());

        // Embedding
        info.put("embedding.generator.bean", embeddingGenerator.getClass().getName());
        info.put("embedding.storage.bean", embeddingStorage.getClass().getName());
        info.put("embedding.pgvector.host", pgHost);
        info.put("embedding.pgvector.tableName", tableName);
        info.put("embedding.pgvector.dimensions", dimensions);

        List<String> docIds = embeddingStorage.findAllDocumentIds();
        info.put("embedding.count", docIds.size());
        info.put("embedding.documentIds", docIds.stream()
                .sorted()
                .limit(5)
                .collect(Collectors.toList()));

        // Tokenizer
        info.put("tokenizer.path", tokenizerConfig.getLoadedPath());
        info.put("tokenizer.maxLength", tokenizerConfig.getMaxLength());

        // Chunking
        info.put("chunking.bean", chunkingService.getClass().getName());
        info.put("chunking.chunkSize", chunkSize);
        info.put("chunking.overlap", chunkOverlap);

        // Inference
        info.put("inference.maxResults", maxResults);
        info.put("inference.minScore", minScore);

        return info;
    }

}
