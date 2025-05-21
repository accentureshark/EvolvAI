package org.shark.evolvai.embedding.config;

import org.shark.evolvai.embedding.domain.service.TextChunkingService;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties({TokenizerProperties.class, ChunkingProperties.class})
public class EmbeddingConfig {

    @Value("${embedding.storage.type:inMemory}")
    private String storageType;

    @Value("${embedding.generator.type:onnx}")
    private String generatorType;

    @Bean
    @Primary
    public EmbeddingStorage embeddingStorage(
            @Qualifier("inMemoryEmbeddingStorage") EmbeddingStorage inMemoryStorage,
            @Qualifier("pgVectorEmbeddingStorage") EmbeddingStorage pgVectorStorage) {
        if ("pgVector".equalsIgnoreCase(storageType)) {
            return pgVectorStorage;
        }
        return inMemoryStorage;
    }

    @Bean
    @Primary
    public EmbeddingGenerator embeddingGenerator(
            @Qualifier("onnxEmbeddingGenerator") EmbeddingGenerator onnxGenerator,
            @Qualifier("ollamaEmbeddingGenerator") EmbeddingGenerator ollamaGenerator) {
        return switch (generatorType.toLowerCase()) {
            case "onnx" -> onnxGenerator;
            case "ollama" -> ollamaGenerator;
            default -> throw new IllegalArgumentException("Generador no soportado: " + generatorType);
        };
    }

    @Bean
    public TextChunkingService textChunkingService(ChunkingProperties properties) {
        return new TextChunkingService(properties.getSize(), properties.getOverlap());
    }
}
