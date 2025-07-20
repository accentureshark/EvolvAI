package org.shark.evolvai.mcp.controller;

import org.shark.evolvai.mcp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * REST controller for handling chat requests through MCP.
 */
@RestController
@RequestMapping("/api/mcp/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
        logger.info("ChatController initialized");
    }
    
    /**
     * Send a message to the chat service.
     * 
     * @param request the chat request containing the message
     * @return the chat response
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody ChatRequest request) {
        logger.debug("Received chat request: {}", request.getMessage());
        
        try {
            String response = chatService.processMessage(request.getMessage());
            
            Map<String, Object> responseBody = Map.of(
                "success", true,
                "message", response,
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(responseBody);
            
        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", "Failed to process message",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get the status of the chat service.
     * 
     * @return the service status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean isAvailable = chatService.isServiceAvailable();
        
        Map<String, Object> status = Map.of(
            "available", isAvailable,
            "status", isAvailable ? "online" : "offline",
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Initialize the chat service.
     * 
     * @return initialization result
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initialize() {
        try {
            chatService.initialize();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Chat service initialized successfully",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error initializing chat service", e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", "Failed to initialize chat service",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Request DTO for chat messages.
     */
    public static class ChatRequest {
        private String message;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}