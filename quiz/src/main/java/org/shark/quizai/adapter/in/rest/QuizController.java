package org.shark.quizai.adapter.in.rest;

import org.shark.quizai.application.service.QuizService;
import org.shark.quizai.domain.model.QuizResponseRequest;
import org.shark.quizai.domain.model.QuizDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadDefinition(@RequestBody QuizDefinition definition) {
        quizService.saveDefinition(definition);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<QuizDefinition> getQuiz(@PathVariable String documentId) {
        return ResponseEntity.ok(quizService.getDefinition(documentId));
    }

    @PostMapping("/response")
    public ResponseEntity<String> processResponse(@RequestBody QuizResponseRequest request) {
        String resultado = quizService.processResponse(request);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/list")
    public List<String> listAllQuizzes() {
        return quizService.listDocumentIds();
    }

    @GetMapping("/step")
    public ResponseEntity<QuizDefinition.QuizStep> getStep(@RequestParam String quizId, @RequestParam int step) {
        QuizDefinition def = quizService.getDefinition(quizId);
        return def.getSteps().stream()
                .filter(s -> s.getStep() == step)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

