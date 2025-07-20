package org.shark.evolvai.mcp;

import org.shark.evolvai.mcp.config.McpProperties;
import org.shark.evolvai.mcp.service.ChatService;
import org.shark.evolvai.mcp.service.McpServerService;
import org.shark.evolvai.mcp.controller.ChatController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration to test MCP bean creation and dependency injection.
 */
@Configuration
public class McpTestConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(McpTestConfig.class);
    
    @Bean
    public CommandLineRunner mcpBeanTest(McpProperties mcpProperties, 
                                        McpServerService mcpServerService,
                                        ChatService chatService,
                                        ChatController chatController) {
        return args -> {
            logger.info("=== MCP Bean Dependency Injection Test ===");
            logger.info("McpProperties bean: {}", mcpProperties.getClass().getSimpleName());
            logger.info("McpProperties enabled: {}", mcpProperties.isEnabled());
            logger.info("McpProperties server URL: {}", mcpProperties.getServerUrl());
            logger.info("McpServerService bean: {}", mcpServerService.getClass().getSimpleName());
            logger.info("ChatService bean: {}", chatService.getClass().getSimpleName());
            logger.info("ChatController bean: {}", chatController.getClass().getSimpleName());
            logger.info("=== MCP Bean Dependency Injection Test PASSED ===");
        };
    }
}