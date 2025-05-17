package org.shark.evolvai.chathistory.adapter.in;

import dev.langchain4j.data.message.ChatMessage;
import org.shark.evolvai.chathistory.service.ChatMemoryService;
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

    @PostMapping("/{id}")
    public void updateMessages(@PathVariable String id, @RequestBody List<ChatMessage> messages) {
        chatMemoryService.updateMessages(id, messages);
    }

    @DeleteMapping("/{id}")
    public void deleteMessages(@PathVariable String id) {
        chatMemoryService.deleteMessages(id);
    }
}