package org.shark.evolvai.embedding.config;

import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
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

        if ("pgVector".equals(storageType)) {
            return pgVectorStorage;
        }
        return inMemoryStorage;
    }

    @Bean
    @Primary
    public EmbeddingGenerator embeddingGenerator(
            @Qualifier("onnxEmbeddingGenerator") EmbeddingGenerator onnxGenerator) {
        // Aquí se pueden agregar más generadores en el futuro (Titan, etc.)
        if ("onnx".equals(generatorType)) {
            return onnxGenerator;
        }
        return onnxGenerator; // Por defecto usa ONNX
    }
}