package org.shark.evolvai.chathistory.adapter.in;

import dev.langchain4j.data.message.ChatMessage;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat-memory")
public class ChatMemoryController {

    private final ChatMemoryService chatMemoryService;

    public ChatMemoryController(ChatMemoryService chatMemoryService) {
        this.chatMemoryService = chatMemoryService;
    }

    @GetMapping("/{id}")
    public List<ChatMessage> getMessages(@PathVariable String id) {
        return chatMemoryService.getMessages(id);
    }

    @GetMapping
    public List<ChatMessage> getAllMessages() {
        return chatMemoryService.getAllMessages();
    }
}
