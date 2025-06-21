package org.shark.evolvai.inference.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class QueryResponse {

    @Schema(
        description = "Respuesta generada por el modelo",
        example = "La capital de Francia es París."
    )
    private String answer;

    @Schema(
        description = "Documentos similares encontrados"
    )
    private List<EmbeddingMatchDto> matches;

    @Schema(
        description = "ID de conversación para mantener el contexto",
        example = "conv-123456"
    )
    private String conversationId;
}