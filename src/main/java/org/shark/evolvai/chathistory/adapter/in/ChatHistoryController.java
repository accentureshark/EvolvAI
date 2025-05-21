
package org.shark.evolvai.chathistory.adapter.in;

import org.shark.evolvai.chathistory.adapter.out.ChatHistoryJpaRepository;
import org.shark.evolvai.chathistory.adapter.out.ChatHistoryEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat-history")
public class ChatHistoryController {

    private final ChatHistoryJpaRepository repository;

    public ChatHistoryController(ChatHistoryJpaRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{conversationId}")
    public List<ChatHistoryEntity> getHistory(@PathVariable String conversationId) {
        return repository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    @PostMapping("/")
    public ChatHistoryEntity save(@RequestBody ChatHistoryEntity entity) {
        return repository.save(entity);
    }
}