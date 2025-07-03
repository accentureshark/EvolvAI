package org.shark.quizai.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "quiz_definition", schema = "quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDefinition {
    @Id
    private String documentId;
    private String tipo;
    private String tema;
    private String version;

    @Transient // O usa @ElementCollection si quieres persistir los steps
    private List<QuizStep> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizStep {
        private int step;
        private String id;
        private String texto;
        private List<String> opciones;
    }
}