package org.shark.evolvai.llm.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.metrics.MonitoredEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
@Tag(name = "LLM Prompt", description = "Endpoints para gestionar el prompt del modelo de lenguaje")
public class LlmPromptController {

    private final RagProperties ragProperties;

    private String overridePrompt;

    @Operation(
            summary = "Obtiene el prompt actual",
            description =
                "Retorna el prompt actual del sistema, ya sea el personalizado o el por defecto"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Prompt recuperado exitosamente"
            ),
        @ApiResponse(
            responseCode = "500",
            description = "Error inesperado del servidor"
            )
    })
    @GetMapping("/prompt")
    @MonitoredEndpoint(name = "api.llm.getPrompt")
    public ResponseEntity<String> getPrompt() {
        String prompt = (overridePrompt != null && !overridePrompt.isBlank())
                ? overridePrompt
                : ragProperties.getLlm().getPrompt();
        return ResponseEntity.ok(prompt);
    }

    @Operation(
            summary = "Establece un nuevo prompt",
            description = "Configura un prompt personalizado que sobrescribe el prompt por defecto"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Prompt actualizado exitosamente"
            ),
        @ApiResponse(
            responseCode = "500",
            description = "Error inesperado del servidor"
            )
    })
    @PostMapping("/prompt")
    @MonitoredEndpoint(name = "api.llm.setPrompt")
    public ResponseEntity<Void> setPrompt(@RequestBody String newPrompt) {
        this.overridePrompt = newPrompt.trim();
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Resetea el prompt",
            description =
                "Elimina el prompt personalizado y restaura el prompt por defecto del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Prompt reseteado exitosamente al valor por defecto"
            ),
        @ApiResponse(
            responseCode = "500",
            description = "Error inesperado del servidor"
            )
    })
    @DeleteMapping("/prompt")
    @MonitoredEndpoint(name = "api.llm.resetPrompt")
    public ResponseEntity<Void> resetPrompt() {
        this.overridePrompt = null;
        return ResponseEntity.ok().build();
    }
}

