package org.shark.evolvai.llm.adapter.in.rest;

import lombok.RequiredArgsConstructor;

import org.shark.evolvai.config.RagProperties;
import org.shark.evolvai.metrics.MonitoredEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmPromptController {

    private final RagProperties ragProperties;

    private String overridePrompt;

    @GetMapping("/prompt")
    @MonitoredEndpoint(name = "api.llm.getPrompt" )
    public ResponseEntity<String> getPrompt() {
        String prompt = (overridePrompt != null && !overridePrompt.isBlank())
                ? overridePrompt
                : ragProperties.getLlm().getPrompt();
        return ResponseEntity.ok(prompt);
    }

    @PostMapping("/prompt")
    @MonitoredEndpoint(name = "api.llm.setPrompt")
    public ResponseEntity<Void> setPrompt(@RequestBody String newPrompt) {
        this.overridePrompt = newPrompt.trim();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/prompt")
    @MonitoredEndpoint(name = "api.llm.resetPrompt")
    public ResponseEntity<Void> resetPrompt() {
        this.overridePrompt = null;
        return ResponseEntity.ok().build();
    }
}

