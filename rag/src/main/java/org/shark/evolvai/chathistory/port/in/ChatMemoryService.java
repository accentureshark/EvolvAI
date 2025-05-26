package org.shark.evolvai.chathistory.port.in;

import dev.langchain4j.data.message.ChatMessage;
import java.util.List;

public interface ChatMemoryService {
    List<ChatMessage> getMessages(String conversationId);
    void updateMessages(String conversationId, List<ChatMessage> messages);
    List<ChatMessage> getAllMessages();
}
