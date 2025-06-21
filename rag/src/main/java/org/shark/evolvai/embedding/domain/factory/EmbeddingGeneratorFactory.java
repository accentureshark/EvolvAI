package org.shark.evolvai.embedding.domain.factory;

import java.util.List;

import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.embedding.port.out.EmbeddingGenerator;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingGeneratorFactory {

    private final EmbeddingGenerator generator;

    public EmbeddingGeneratorFactory(
        List<EmbeddingGenerator> generators, RagProperties properties
    ) {
        String provider = properties.getEmbedding().getGenerator().getProvider().toLowerCase();

        this.generator = generators.stream()
                .filter(g -> g.getClass().getSimpleName().toLowerCase().contains(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                    "No se encontró un EmbeddingGenerator para: " + provider)
                );
    }

    public EmbeddingGenerator get() {
        return generator;
    }
}
