package org.shark.evolvai.chatmemory.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Clase intermedia para almacenamiento
    public static class StoredMessage {
        public String type;
        public String text;

        // Constructor por Jackson
        public StoredMessage() {}

        public StoredMessage(String type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    public static String serializeMessages(List<ChatMessage> messages) {
        try {
            List<StoredMessage> stored = messages.stream()
                    .map(msg -> {
                        if (msg instanceof UserMessage u) return new StoredMessage("user", u.text());
                        if (msg instanceof AiMessage a) return new StoredMessage("ai", a.text());
                        if (msg instanceof SystemMessage s) return new StoredMessage("system", s.text());
                        throw new IllegalArgumentException("Unsupported message type: " + msg.getClass());
                    })
                    .toList();

            String result = objectMapper.writeValueAsString(stored);
            logger.debug("Serialización exitosa: {}", result);
            return result;
        } catch (JsonProcessingException e) {
            logger.error("Error al serializar mensajes", e);
            throw new RuntimeException("Error al serializar mensajes", e);
        }
    }

    public static List<ChatMessage> deserializeMessages(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<StoredMessage> stored = objectMapper.readValue(json, new TypeReference<>() {});
            List<ChatMessage> result = stored.stream()
                    .map(s -> switch (s.type) {
                        case "user" -> new UserMessage(s.text);
                        case "ai" -> new AiMessage(s.text);
                        case "system" -> new SystemMessage(s.text);
                        default -> throw new IllegalArgumentException("Tipo desconocido: " + s.type);
                    })
                    .toList();
            logger.debug("Deserialización exitosa: {} mensajes", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error al deserializar mensajes", e);
            return new ArrayList<>();
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
