package org.shark.evolvai.admin.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "embedding_source", schema = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddingSource {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String path; // File location, S3 URI, etc.
}
