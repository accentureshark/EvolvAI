package org.shark.evolvai.admin.model.prompt;


import jakarta.persistence.*;
import lombok.*;
import org.shark.evolvai.admin.model.common.MessageRole;

import java.util.UUID;

@Entity
@Table(name = "prompt_message", schema = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptMessage {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;
}
