package org.shark.evolvai.embedding.provider;

import java.util.List;
import java.util.stream.Collectors;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.shark.evolvai.embedding.port.out.EmbeddingGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("ollamaEmbeddingGenerator")
public class OllamaEmbeddingGenerator implements EmbeddingGenerator {

    private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingGenerator.class);
    private final EmbeddingModel embeddingModel;

    public OllamaEmbeddingGenerator(
            @Value("${rag.embedding.ollama.base-url}") String baseUrl,
            @Value("${rag.embedding.generator.model}") String modelName
    ) {
        this.embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
        log.info(
            "OllamaEmbeddingGenerator inicializado con baseUrl={} y model={}",
            baseUrl,
            modelName
        );
    }

    @Override
    public Embedding generateEmbedding(String text) {
        try {
            Response<Embedding> embeddingResponse = embeddingModel.embed(text);
            return embeddingResponse.content();
        } catch (Exception e) {
            log.error("Error generando embedding con Ollama", e);
            throw new RuntimeException("Error generando embedding con Ollama", e);
        }
    }

    @Override
    public Embedding generateEmbedding(TextSegment segment) {
        return generateEmbedding(segment.text());
    }

    @Override
    public Embedding generateEmbedding(int[] inputIds, int[] attentionMask) {
        throw new UnsupportedOperationException("No soportado por OllamaEmbeddingGenerator");
    }

    @Override
    public List<Embedding> generateEmbeddings(List<TextSegment> segments) {
        return segments.stream()
                .map(this::generateEmbedding)
                .collect(Collectors.toList());
    }
}