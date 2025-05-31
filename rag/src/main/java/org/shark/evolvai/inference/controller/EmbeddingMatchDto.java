package org.shark.evolvai.inference.controller;

import java.util.Collections;
import java.util.Map;

public class EmbeddingMatchDto {
    private final double score;
    private final String embeddingId;
    private final float[] embedding;
    private final String text;
    private final Map<String, Object> metadata;

    // Constructor principal con metadatos
    public EmbeddingMatchDto(double score, String embeddingId, float[] embedding, String text, Map<String, Object> metadata) {
        this.score = score;
        this.embeddingId = embeddingId;
        this.embedding = embedding;
        this.text = text;
        this.metadata = metadata != null ? metadata : Collections.emptyMap(); // evita null
    }

    // Constructor alternativo sin metadatos (por compatibilidad)
    public EmbeddingMatchDto(double score, String embeddingId, float[] embedding, String text) {
        this(score, embeddingId, embedding, text, Collections.emptyMap());
    }

    public double getScore() {
        return score;
    }

    public String getEmbeddingId() {
        return embeddingId;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public String getText() {
        return text;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
