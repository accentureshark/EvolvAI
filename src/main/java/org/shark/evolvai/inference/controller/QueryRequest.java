package org.shark.evolvai.inference.controller;

import io.swagger.v3.oas.annotations.media.Schema;

public class QueryRequest {

    @Schema(
            description = "The query string provided by the user",
            example = "¿Cuál es la capital de Francia?"
    )
    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
