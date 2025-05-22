package org.shark.evolvai.embedding.adapter.input.rest;

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
}