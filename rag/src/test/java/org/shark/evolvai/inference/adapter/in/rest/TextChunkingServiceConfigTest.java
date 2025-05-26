package org.shark.evolvai.inference.adapter.in.rest;



import org.junit.jupiter.api.Test;
import org.shark.evolvai.embedding.config.ChunkingProperties;
import org.shark.evolvai.embedding.config.EmbeddingConfig;
import org.shark.evolvai.embedding.domain.service.TextChunkingService;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextChunkingServiceConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EmbeddingConfig.class, ChunkingProperties.class)
            .withPropertyValues(
                    "rag.chunk.size=250",
                    "rag.chunk.overlap=30",
                    "rag.chunk.enrich-with=nivel,area,documentId"
            );

    @Test
    void testTextChunkingServiceBeanWithYamlProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TextChunkingService.class);
            TextChunkingService service = context.getBean(TextChunkingService.class);

            //assertThat(service.getChunkSize()).isEqualTo(250);
            //assertThat(service.getOverlap()).isEqualTo(30);
            //assertThat(service.getEnrichKeys()).containsExactly("nivel", "area", "documentId");
        });
    }
}
