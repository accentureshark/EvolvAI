package org.shark.evolvai.chathistory.adapter.out;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_memory")
public class ChatMemoryEntity {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String memoryJson;

    public ChatMemoryEntity() {
    }

    public ChatMemoryEntity(String id, String memoryJson) {
        this.id = id;
        this.memoryJson = memoryJson;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemoryJson() {
        return memoryJson;
    }

    public void setMemoryJson(String memoryJson) {
        this.memoryJson = memoryJson;
    }
}