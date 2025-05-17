package org.shark.evolvai.inference.controller;

import dev.langchain4j.store.embedding.EmbeddingMatch;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class QueryResponse {

    @Schema(description = "Respuesta generada por el modelo", example = "La capital de Francia es París.")
    private String answer;

    @Schema(description = "Documentos similares encontrados")
    private List<EmbeddingMatch<String>> matches;

    public QueryResponse(String answer, List<EmbeddingMatch<String>> matches) {
        this.answer = answer;
        this.matches = matches;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<EmbeddingMatch<String>> getMatches() {
        return matches;
    }

    public void setMatches(List<EmbeddingMatch<String>> matches) {
        this.matches = matches;
    }
}