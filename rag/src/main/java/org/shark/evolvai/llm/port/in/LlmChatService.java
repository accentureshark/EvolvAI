package org.shark.evolvai.llm.port.in;

import java.util.List;

import dev.langchain4j.data.message.ChatMessage;

public interface LlmChatService {
    List<ChatMessage> chat(String memoryId, List<ChatMessage> messages);
}