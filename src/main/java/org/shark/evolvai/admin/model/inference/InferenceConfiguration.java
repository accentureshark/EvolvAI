package org.shark.evolvai.admin.model.inference;


import jakarta.persistence.*;
import lombok.*;
import org.shark.evolvai.admin.model.EmbeddingSource;
import org.shark.evolvai.admin.model.common.InferenceContextType;
import org.shark.evolvai.admin.model.prompt.Prompt;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inference_configuration", schema = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InferenceConfiguration {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InferenceContextType contextType;

    @Column(nullable = false)
    private UUID contextId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiRole aiRole;

    @ManyToMany
    @JoinTable(
            name = "inference_configuration_prompt",
            schema = "admin",
            joinColumns = @JoinColumn(name = "config_id"),
            inverseJoinColumns = @JoinColumn(name = "prompt_id")
    )
    private List<Prompt> prompts;

    @ManyToMany
    @JoinTable(
            name = "inference_configuration_embedding_source",
            schema = "admin",
            joinColumns = @JoinColumn(name = "config_id"),
            inverseJoinColumns = @JoinColumn(name = "source_id")
    )
    private List<EmbeddingSource> embeddingSources;
}
