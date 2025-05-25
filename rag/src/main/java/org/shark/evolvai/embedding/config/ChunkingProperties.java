package org.shark.evolvai.embedding.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "rag.chunk")
public class ChunkingProperties {

    private int size;
    private int overlap;
    private List<String> enrichWith = new ArrayList<>();

    public int getSize() {
        return size;
    }

    public int getOverlap() {
        return overlap;
    }

    public List<String> getEnrichWith() {
        return enrichWith;
    }
}