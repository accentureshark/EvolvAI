package org.shark.evolvai.chathistory.adapter.out;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_memory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemoryEntity {
    @Id
    private String id;

    @Lob
    @Column(name = "memory_json", columnDefinition = "TEXT")
    private String memoryJson;
}
