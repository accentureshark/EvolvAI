package org.shark.evolvai.embedding.adapter.input.rest;

public class DocumentRequest {
    private String id;
    private String text;

    // Constructor por defecto para deserialización JSON
    public DocumentRequest() {
    }

    // Constructor con parámetros para facilitar la creación de instancias
    public DocumentRequest(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
