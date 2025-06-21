package org.shark.evolvai.chathistory.adapter.out;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_memory")
@Data
@NoArgsConstructor

public class ChatMemoryEntity {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String memoryJson;

    public ChatMemoryEntity(String id, String memoryJson) {
        this.id = id;
        this.memoryJson = memoryJson;
    }

    public String getId() {
        return id;
    }

    public String getMemoryJson() {
        return memoryJson;
    }
}
