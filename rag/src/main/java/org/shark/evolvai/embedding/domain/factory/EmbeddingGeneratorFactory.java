package org.shark.evolvai.embedding.domain.factory;

import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbeddingGeneratorFactory {

    private final EmbeddingGenerator generator;

    public EmbeddingGeneratorFactory(
            List<EmbeddingGenerator> generators,
            @Value("${embedding.generator.type:onnx}") String provider,
            Environment env
    ) {
        String type = provider.toLowerCase();
        this.generator = generators.stream()
                .filter(g -> g.getClass().getSimpleName().toLowerCase().contains(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró un EmbeddingGenerator para: " + type));
    }

    public EmbeddingGenerator get() {
        return generator;
    }
}