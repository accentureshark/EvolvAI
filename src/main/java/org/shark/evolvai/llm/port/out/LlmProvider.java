package org.shark.evolvai.llm.port.out;

import dev.langchain4j.data.message.ChatMessage;
import java.util.List;

public interface LlmProvider {
    ChatMessage send(List<ChatMessage> messages);
    String generateResponse(String context, String query);
    String generateResponseWithHistory(String context, String query, String conversationHistory);


}
