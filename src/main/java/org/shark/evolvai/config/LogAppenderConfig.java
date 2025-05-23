package org.shark.evolvai.config;

import jakarta.annotation.PostConstruct;
import org.shark.evolvai.logging.WebSocketLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class LogAppenderConfig {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    public void init() {
        WebSocketLogAppender.setMessagingTemplate(messagingTemplate);
    }
}
