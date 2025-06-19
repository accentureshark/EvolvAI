package org.shark.evolvai.chathistory.port.in;

import java.util.List;

import dev.langchain4j.data.message.ChatMessage;

public interface ChatMemoryService {
    List<ChatMessage> getMessages(String conversationId);

    void updateMessages(String conversationId, List<ChatMessage> messages);

    List<ChatMessage> getAllMessages();
}
