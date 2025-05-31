package org.shark.evolvai.embedding.config;

import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.domain.service.TextChunkingService;
import org.shark.evolvai.embedding.port.out.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.out.EmbeddingStorage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class EmbeddingConfig {

    private final RagProperties ragProperties;

    public EmbeddingConfig(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
    }

    @Bean
    @Primary
    public EmbeddingStorage embeddingStorage(
            @Qualifier("pgVectorEmbeddingStorage") EmbeddingStorage pgVectorStorage) {
        String storageType = ragProperties.getEmbedding().getStorage().getType();
        if ("pgVector".equalsIgnoreCase(storageType)) {
            return pgVectorStorage;
        }
        throw new IllegalStateException("Tipo de almacenamiento no soportado: " + storageType);
    }

    @Bean
    @Primary
    public EmbeddingGenerator embeddingGenerator(
            List<EmbeddingGenerator> generators) {
        String type = ragProperties.getEmbedding().getGenerator().getProvider();
        return generators.stream()
                .filter(g -> g.getClass().getSimpleName().toLowerCase().contains(type.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró un EmbeddingGenerator para: " + type));
    }

    @Bean
    public TextChunkingService textChunkingService() {
        int size = ragProperties.getEmbedding().getChunking().getSize();
        int overlap = ragProperties.getEmbedding().getChunking().getOverlap();
        List<String> enrichWith = ragProperties.getEmbedding().getChunking().getEnrichWith();
        return new TextChunkingService(size, overlap, enrichWith != null ? enrichWith : List.of());
    }
}
