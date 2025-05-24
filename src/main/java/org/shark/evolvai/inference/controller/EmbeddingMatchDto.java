package org.shark.evolvai.inference.controller;


public class EmbeddingMatchDto {
    private final double score;
    private final String embeddingId;
    private final float[] embedding;
    private final String text;

    public EmbeddingMatchDto(double score, String embeddingId, float[] embedding, String text) {
        this.score = score;
        this.embeddingId = embeddingId;
        this.embedding = embedding;
        this.text = text;
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
}
