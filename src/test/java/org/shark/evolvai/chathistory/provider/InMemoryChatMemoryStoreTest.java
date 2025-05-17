package org.shark.evolvai.chathistory.provider;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shark.evolvai.chathistory.util.JsonUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryChatMemoryStoreTest {

    private InMemoryChatMemoryStore chatMemoryStore;
    private final String testMemoryId = "test-memory-id";

    @BeforeEach
    void setUp() {
        chatMemoryStore = new InMemoryChatMemoryStore();
        chatMemoryStore.clearAll(); // Asegura limpieza entre tests
    }

    @Test
    void testBasicOperations() {
        // Verificar que inicialmente no hay mensajes
        List<ChatMessage> initialMessages = chatMemoryStore.getMessages(testMemoryId);
        assertTrue(initialMessages.isEmpty(), "Inicialmente no debería haber mensajes");

        // Crear mensajes de prueba
        UserMessage userMessage = new UserMessage("Hola, ¿cómo estás?");
        AiMessage aiMessage = new AiMessage("Estoy bien, ¿en qué puedo ayudarte?");
        List<ChatMessage> messages = List.of(userMessage, aiMessage);

        // Actualizar mensajes
        chatMemoryStore.updateMessages(testMemoryId, messages);

        // Verificar que los mensajes se guardaron correctamente
        List<ChatMessage> retrievedMessages = chatMemoryStore.getMessages(testMemoryId);
        assertEquals(2, retrievedMessages.size(), "Debería haber 2 mensajes guardados");
        assertEquals(userMessage.text(), retrievedMessages.get(0).text());
        assertEquals(aiMessage.text(), retrievedMessages.get(1).text());

        // Eliminar mensajes
        chatMemoryStore.deleteMessages(testMemoryId);
        List<ChatMessage> messagesAfterDeletion = chatMemoryStore.getMessages(testMemoryId);
        assertTrue(messagesAfterDeletion.isEmpty(), "Después de eliminar no debería haber mensajes");
    }

    @Test
    void testGetRelevantMemory() {
        // Guardar mensajes con un ID que incluya "consulta1"
        chatMemoryStore.updateMessages("memoria-consulta1", List.of(new AiMessage("test")));

        List<String> relevantMemory = chatMemoryStore.getRelevantMemory("consulta1");
        assertFalse(relevantMemory.isEmpty(), "Debería encontrar memoria relevante para consulta1");
        assertTrue(relevantMemory.contains("memoria-consulta1"));
    }

    @Test
    void testJsonUtilSerializationWithTyping() {
        // Crear mensajes de prueba
        List<ChatMessage> original = List.of(
                new UserMessage("Hola"),
                new AiMessage("¿En qué puedo ayudarte?")
        );

        // Serializar
        String json = JsonUtil.serializeMessages(original);
        System.out.println("💡 JSON serializado:\n" + json);

        // Verificar que contiene los campos esperados
        assertTrue(json.contains("\"type\":\"user\""), "El JSON debe contener el tipo user");
        assertTrue(json.contains("\"type\":\"ai\""), "El JSON debe contener el tipo ai");

        // Deserializar
        List<ChatMessage> parsed = JsonUtil.deserializeMessages(json);

        // Validar
        assertEquals(2, parsed.size(), "Se deben deserializar 2 mensajes");
        assertTrue(parsed.get(0) instanceof UserMessage, "El primer mensaje debe ser UserMessage");
        assertTrue(parsed.get(1) instanceof AiMessage, "El segundo mensaje debe ser AiMessage");
        assertEquals("Hola", ((UserMessage) parsed.get(0)).text());
        assertEquals("¿En qué puedo ayudarte?", ((AiMessage) parsed.get(1)).text());
    }


}
