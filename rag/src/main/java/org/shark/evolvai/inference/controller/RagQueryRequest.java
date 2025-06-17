package org.shark.evolvai.inference.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import org.shark.evolvai.inference.model.DataSourceInfo;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Schema(description = "Prompt personalizado para mejorar la respuesta del modelo", example = "Responde de manera concisa y profesional")
    private String customPrompt;

    @Schema(description = "ID del documento fuente a utilizar como filtro", example = "plan-carrera")
    private String documentId;

    @Schema(description = "Metadatos adicionales para filtrar los documentos relevantes", example = "{\"organizacion\":\"Accenture\"}")
    private Map<String, String> contextMetadata;

    @NotNull(message = "La fuente de datos no puede ser nula")
    @Valid
    @Schema(description = "Información de la fuente de datos (VECTOR_DB, MCP_SERVER, etc.)", required = true)
    private DataSourceInfo source;
}
