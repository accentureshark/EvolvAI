package org.shark.evolvai.chathistory.integration;

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
import org.shark.evolvai.chathistory.service.ChatMemoryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMemoryIntegrationTest {

    private final String memoryId = "test-memory";
    @Mock
    ChatMemoryStore chatMemoryStore;
    @InjectMocks
    ChatMemoryService chatMemoryService;

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
        assertInstanceOf(UserMessage.class, messages.get(0));
        assertEquals("Hola", messages.get(0).text());
    }
}
