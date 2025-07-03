package org.shark.quizai.domain.port.out;



import org.shark.quizai.domain.model.QuizResponseRequest;

public interface InferenceClient {
    String inferResult(QuizResponseRequest request);
}

