package org.shark.evolvai.embedding.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {
    private String id;
    private String text;
    private String customPrompt;

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getCustomPrompt() {
        return customPrompt;
    }
}