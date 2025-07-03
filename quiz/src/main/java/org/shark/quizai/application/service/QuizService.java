package org.shark.quizai.application.service;

import org.shark.quizai.domain.model.QuizDefinition;
import org.shark.quizai.domain.model.QuizResponseRequest;
import org.shark.quizai.domain.port.out.QuizRepository;
import org.shark.quizai.domain.port.out.InferenceClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final InferenceClient inferenceClient;

    public QuizService(QuizRepository quizRepository, InferenceClient inferenceClient) {
        this.quizRepository = quizRepository;
        this.inferenceClient = inferenceClient;
    }

    public void saveDefinition(QuizDefinition definition) {
        quizRepository.save(definition);
    }

    public QuizDefinition getDefinition(String documentId) {
        Optional<QuizDefinition> def = quizRepository.findById(documentId);
        return def.orElse(null);
    }

    public String processResponse(QuizResponseRequest request) {
        // Si tienes una entidad para respuestas, aquí deberías guardarla.
        // quizRepository.saveResponse(request); // Solo si implementas esto.
        return inferenceClient.inferResult(request);
    }

    public List<String> listDocumentIds() {
        return quizRepository.findAll()
                .stream()
                .map(QuizDefinition::getDocumentId)
                .collect(Collectors.toList());
    }
}
