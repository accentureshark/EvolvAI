package org.shark.evolvai.chathistory.adapter.in.rest;

import lombok.Data;

@Data
public class ConversationDto {
    private String id;
    private String preview;

    public ConversationDto(String id, String preview) {
        this.id = id;
        this.preview = preview;
    }
}
