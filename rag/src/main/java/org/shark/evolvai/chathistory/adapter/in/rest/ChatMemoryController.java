package org.shark.evolvai.chathistory.adapter.in.rest;

import dev.langchain4j.data.message.ChatMessage;
import org.shark.evolvai.chathistory.adapter.out.PostgresChatMemoryStore;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<ChatMessageDto> getAllMessages() {
        Logger logger = LoggerFactory.getLogger(ChatMemoryController.class);
        return chatMemoryService.getAllMessages().stream()
                .peek(msg -> logger.info("Mensaje: {}", msg))
                .map(msg -> new ChatMessageDto(
                        msg.getClass().getSimpleName(), // o el campo adecuado para el rol
                        msg.toString() // o el método adecuado para el contenido
                ))
                .toList();
    }

}
