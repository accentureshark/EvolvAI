package org.shark.evolvai.inference.controller;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RagQueryRequest {

    @NotBlank(message = "La consulta no puede estar vacía")
    @Schema(description = "Consulta del usuario", example = "¿Cuáles son los componentes principales de la arquitectura RAG?", required = true)
    private String query;

    @Schema(description = "ID de conversación para mantener contexto entre consultas", example = "conv-123456")
    private String conversationId;

    @Min(value = 1, message = "El número mínimo de documentos a recuperar es 1")
    @Max(value = 10, message = "El número máximo de documentos a recuperar es 10")
    @Schema(description = "Número de documentos similares a recuperar", example = "3", defaultValue = "5")
    private int maxResults = 5;

    @Min(value = 0, message = "El umbral de similitud debe ser mayor o igual a 0")
    @Max(value = 1, message = "El umbral de similitud debe ser menor o igual a 1")
    @Schema(description = "Umbral mínimo de similitud (0-1)", example = "0.7", defaultValue = "0.7")
    private double minSimilarity = 0.7;

    @Schema(description = "Incluir documentos recuperados en la respuesta", defaultValue = "true")
    private boolean includeMatches = true;

    // Constructores, getters y setters
    public RagQueryRequest() {
    }

    public RagQueryRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public double getMinSimilarity() {
        return minSimilarity;
    }

    public void setMinSimilarity(double minSimilarity) {
        this.minSimilarity = minSimilarity;
    }

    public boolean isIncludeMatches() {
        return includeMatches;
    }

    public void setIncludeMatches(boolean includeMatches) {
        this.includeMatches = includeMatches;
    }
}