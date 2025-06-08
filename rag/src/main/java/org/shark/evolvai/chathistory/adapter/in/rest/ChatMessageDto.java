package org.shark.evolvai.chathistory.adapter.in.rest;

import lombok.Data;

@Data
public class ChatMessageDto {
    public String role;
    public String content;

    public ChatMessageDto(String role, String content) {
        this.role = role;
        this.content = content;
    }
}