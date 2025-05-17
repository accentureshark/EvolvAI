package org.shark.evolvai.llm.port.in;

import dev.langchain4j.data.message.ChatMessage;
import java.util.List;

public interface LlmChatService {
    List<ChatMessage> chat(String memoryId, List<ChatMessage> messages);
}