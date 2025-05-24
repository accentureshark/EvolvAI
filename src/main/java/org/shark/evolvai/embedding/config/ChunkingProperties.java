package org.shark.evolvai.embedding.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "embedding.chunking")
public class ChunkingProperties {
    private int size;
    private int overlap;
}