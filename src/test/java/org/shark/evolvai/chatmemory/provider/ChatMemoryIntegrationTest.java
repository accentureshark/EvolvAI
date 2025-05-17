package org.shark.evolvai.chatmemory.integration;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shark.evolvai.chatmemory.service.ChatMemoryService;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMemoryIntegrationTest {

    @Mock
    ChatMemoryStore chatMemoryStore;

    @InjectMocks
    ChatMemoryService chatMemoryService;

    private final String memoryId = "test-memory";

    @BeforeEach
    void setUp() {
        when(chatMemoryStore.getMessages(memoryId)).thenReturn(List.of(
                new UserMessage("Hola"),
                new AiMessage("¡Hola! ¿En qué puedo ayudarte?")
        ));
    }

    @Test
    void testChatMemoryServiceWithInMemoryStore() {
        List<ChatMessage> messages = chatMemoryService.getMessages(memoryId);
        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertTrue(messages.get(0) instanceof UserMessage);
        assertEquals("Hola", ((UserMessage) messages.get(0)).text());
    }
}
