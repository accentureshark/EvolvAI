package org.shark.evolvai.llm.adapter.in;

import org.shark.evolvai.llm.port.in.LlmChatService;
import dev.langchain4j.data.message.ChatMessage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/llm")
public class LlmChatController {

    private final LlmChatService llmChatService;

    public LlmChatController(LlmChatService llmChatService) {
        this.llmChatService = llmChatService;
    }

    @PostMapping("/chat/{memoryId}")
    public List<ChatMessage> chat(
            @PathVariable String memoryId,
            @RequestBody List<ChatMessage> messages) {
        return llmChatService.chat(memoryId, messages);
    }
}
