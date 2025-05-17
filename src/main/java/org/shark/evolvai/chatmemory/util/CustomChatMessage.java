package org.shark.evolvai.chatmemory.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;

public class CustomChatMessage implements ChatMessage {
    private final ChatMessageType type;

    @JsonProperty("text") // Cambia el nombre del campo en el JSON
    private final String content;

    public CustomChatMessage(ChatMessageType type, String content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public ChatMessageType type() {
        return type;
    }


    public String text() {
        return content;
    }

    public String getContent() {
        return content;
    }
}
