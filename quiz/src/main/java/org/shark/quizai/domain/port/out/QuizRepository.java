package org.shark.quizai.domain.port.out;

import org.shark.quizai.domain.model.QuizDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<QuizDefinition, String> {
}