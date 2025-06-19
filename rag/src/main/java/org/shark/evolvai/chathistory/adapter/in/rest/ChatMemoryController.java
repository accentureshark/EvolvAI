package org.shark.evolvai.chathistory.adapter.in.rest;

import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import org.shark.evolvai.chathistory.port.in.ChatMemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                        msg.toString() // o el metodo adecuado para el contenido
                ))
                .toList();
    }

}
