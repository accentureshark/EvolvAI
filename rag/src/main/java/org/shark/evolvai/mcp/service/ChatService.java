package org.shark.evolvai.mcp.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling chat operations through MCP.
 */
@Service
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    private final McpServerService mcpServerService;
    
    public ChatService(McpServerService mcpServerService) {
        this.mcpServerService = mcpServerService;
        logger.info("ChatService initialized");
    }
    
    /**
     * Process a chat message and return a response.
     * 
     * @param message the user's message
     * @return the chat response
     */
    public String processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            logger.warn("Received empty or null message");
            return "Please provide a valid message.";
        }
        
        logger.debug("Processing chat message: {}", message);
        
        if (!mcpServerService.isServerAvailable()) {
            logger.warn("MCP server is not available");
            return "Chat service is currently unavailable. Please try again later.";
        }
        
        try {
            String response = mcpServerService.sendMessage(message);
            logger.debug("Received response from MCP server: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error processing chat message", e);
            return "An error occurred while processing your message. Please try again.";
        }
    }
    
    /**
     * Get the status of the chat service.
     * 
     * @return true if the service is available, false otherwise
     */
    public boolean isServiceAvailable() {
        return mcpServerService.isServerAvailable();
    }
    
    /**
     * Initialize the chat service.
     */
    public void initialize() {
        logger.info("Initializing chat service");
        mcpServerService.initialize();
    }
}