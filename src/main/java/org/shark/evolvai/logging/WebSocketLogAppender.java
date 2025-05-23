package org.shark.evolvai.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class WebSocketLogAppender extends AppenderBase<ILoggingEvent> {

    private static SimpMessagingTemplate messagingTemplate;

    public static void setMessagingTemplate(SimpMessagingTemplate template) {
        WebSocketLogAppender.messagingTemplate = template;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/logs", eventObject.getFormattedMessage());
        }
    }
}
