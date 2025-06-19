package org.shark.evolvai.llm.service;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import org.shark.evolvai.llm.port.in.LlmChatService;
import org.shark.evolvai.llm.port.out.LlmProvider;
import org.springframework.stereotype.Service;

@Service
public class LlmChatServiceImpl implements LlmChatService {

    private final LlmProvider llmProvider;


    public LlmChatServiceImpl(LlmProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    @Override
    public List<ChatMessage> chat(String memoryId, List<ChatMessage> messages) {
        List<ChatMessage> history = new ArrayList<>(messages);
        ChatMessage response = llmProvider.send(messages);
        history.add(response);
        return history;
    }
}
