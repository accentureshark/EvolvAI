package org.shark.evolvai.mcp;

import org.junit.jupiter.api.Test;
import org.shark.evolvai.mcp.config.McpProperties;
import org.shark.evolvai.mcp.service.ChatService;
import org.shark.evolvai.mcp.service.McpServerService;
import org.shark.evolvai.mcp.controller.ChatController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify MCP bean dependency injection works correctly.
 */
@SpringBootTest(
    classes = {McpProperties.class, McpServerService.class, ChatService.class, ChatController.class}
)
class McpDependencyInjectionTest {

    @Autowired
    private McpProperties mcpProperties;

    @Autowired
    private McpServerService mcpServerService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatController chatController;

    @Test
    void testMcpPropertiesBeanInjection() {
        assertNotNull(mcpProperties);
        assertEquals("http://localhost:8080", mcpProperties.getServerUrl());
        assertTrue(mcpProperties.isEnabled());
    }

    @Test
    void testMcpServerServiceBeanInjection() {
        assertNotNull(mcpServerService);
        assertNotNull(mcpServerService.getConfiguration());
        assertEquals(mcpProperties, mcpServerService.getConfiguration());
    }

    @Test
    void testChatServiceBeanInjection() {
        assertNotNull(chatService);
        assertTrue(chatService.isServiceAvailable());
    }

    @Test
    void testChatControllerBeanInjection() {
        assertNotNull(chatController);
    }

    @Test
    void testFullDependencyChain() {
        // Test the full dependency chain: ChatController -> ChatService -> McpServerService -> McpProperties
        String message = "Test message";
        String response = chatService.processMessage(message);
        assertNotNull(response);
        assertTrue(response.contains("Echo: " + message));
    }
}