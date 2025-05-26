package org.shark.evolvai.llm.adapter.in.rest;

import lombok.RequiredArgsConstructor;

import org.shark.evolvai.config.RagProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmPromptController {

    private final RagProperties ragProperties;

    private String overridePrompt;

    @GetMapping("/prompt")
    public ResponseEntity<String> getPrompt() {
        String prompt = (overridePrompt != null && !overridePrompt.isBlank())
                ? overridePrompt
                : ragProperties.getLlm().getPrompt();
        return ResponseEntity.ok(prompt);
    }

    @PostMapping("/prompt")
    public ResponseEntity<Void> setPrompt(@RequestBody String newPrompt) {
        this.overridePrompt = newPrompt.trim();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/prompt")
    public ResponseEntity<Void> resetPrompt() {
        this.overridePrompt = null;
        return ResponseEntity.ok().build();
    }
}

