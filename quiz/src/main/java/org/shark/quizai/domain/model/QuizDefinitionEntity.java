package org.shark.quizai.domain.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "quiz_definition", schema = "quiz")
public class QuizDefinitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(unique = true)
    public String documentId;

    public String tipo;
    public String tema;
    public String version;

    @Column(columnDefinition = "jsonb")
    public String stepsJson; // JSON serializado de steps (quiz_steps)
}