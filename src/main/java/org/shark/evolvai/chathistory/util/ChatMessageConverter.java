package org.shark.evolvai.chathistory.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMessageConverter {

    private final ObjectMapper objectMapper;

    public ChatMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<CustomChatMessage> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<CustomChatMessage>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing ChatMessage", e);
        }
    }

    public String toJson(List<ChatMessage> messages) {
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing ChatMessage", e);
        }
    }
}
