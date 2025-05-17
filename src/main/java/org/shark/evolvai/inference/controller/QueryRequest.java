package org.shark.evolvai.inference.controller;

import io.swagger.v3.oas.annotations.media.Schema;

public class QueryRequest {

    @Schema(description = "Consulta del usuario", example = "¿Cuál es la capital de Francia?")
    private String query;

    public QueryRequest() {
    }

    public QueryRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}