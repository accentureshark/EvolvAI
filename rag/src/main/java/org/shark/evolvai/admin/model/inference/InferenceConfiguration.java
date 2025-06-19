package org.shark.evolvai.admin.model.inference;


import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shark.evolvai.admin.model.EmbeddingSource;
import org.shark.evolvai.admin.model.common.InferenceContextType;
import org.shark.evolvai.admin.model.prompt.Prompt;

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
