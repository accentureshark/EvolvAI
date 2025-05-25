package org.shark.evolvai.chathistory.adapter.out;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_memory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemoryEntity {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String memoryJson;
}
