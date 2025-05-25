package org.shark.evolvai.embedding.domain.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.shark.evolvai.embedding.domain.factory.EmbeddingGeneratorFactory;
import org.shark.evolvai.embedding.port.input.EmbeddingUseCase;
import org.shark.evolvai.embedding.port.output.EmbeddingGenerator;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Primary
public class RAGEmbeddingService implements EmbeddingUseCase {

    private static final Logger log = LoggerFactory.getLogger(RAGEmbeddingService.class);
    private static final String DEFAULT_PROMPT = "Genera embeddings para este texto:";

    private final EmbeddingGenerator embeddingGenerator;
    private final EmbeddingStorage embeddingStorage;
    private final TextChunkingService textChunkingService;

    public RAGEmbeddingService(
            EmbeddingGeneratorFactory generatorFactory,
            EmbeddingStorage embeddingStorage,
            TextChunkingService textChunkingService) {
        this.embeddingGenerator = generatorFactory.get();
        this.embeddingStorage = embeddingStorage;
        this.textChunkingService = textChunkingService;
    }

    @Override
    public void indexDocument(String id, String text, String customPrompt) {
        log.info("Indexando documento con id: {}", id);
        String prompt = (customPrompt != null && !customPrompt.isBlank()) ? customPrompt : DEFAULT_PROMPT;

        List<TextSegment> segments = textChunkingService.chunk(text, Map.of("documentId", id));

        for (TextSegment segment : segments) {
            String promptFragment = prompt + "\n" + segment.text();
            Embedding embedding = embeddingGenerator.generateEmbedding(new TextSegment(promptFragment, segment.metadata()));

            String docName = id;
            if (segment.metadata() != null && segment.metadata().asMap().containsKey("chunkIndex")) {
                docName += "/fragment-" + segment.metadata().asMap().get("chunkIndex");
            }

            embeddingStorage.store(docName, embedding, segment.text(), segment.metadata());
            log.info("Fragmento almacenado con id: {}", docName);
        }

        log.info("Documento indexado correctamente: {}", id);
    }

    @Override
    public List<String> listDocumentIds() {
        return embeddingStorage.findAllDocumentIds();
    }

    @Override
    public List<String> findSimilarDocuments(String query, int maxResults, double minScore) {
        log.info("Buscando documentos similares para query: '{}', maxResults: {}, minScore: {}", query, maxResults, minScore);
        Embedding queryEmbedding = embeddingGenerator.generateEmbedding(query);
        List<EmbeddingMatch<String>> matches = embeddingStorage.findSimilar(queryEmbedding, maxResults, minScore);
        log.info("Se encontraron {} documentos similares.", matches.size());
        return matches.stream()
                .map(EmbeddingMatch::embedded)
                .collect(Collectors.toList());
    }

    @Override
    public Embedding generateEmbedding(String text) {
        log.debug("Generando embedding para texto.");
        return embeddingGenerator.generateEmbedding(text);
    }

    @Override
    public Embedding generateEmbedding(int[] inputIds, int[] attentionMask) {
        log.debug("Generando embedding para inputIds y attentionMask.");
        return embeddingGenerator.generateEmbedding(inputIds, attentionMask);
    }

    @Override
    public void removeAllDocuments() {
        log.warn("Eliminando todos los documentos indexados.");
        embeddingStorage.removeAll();
    }
    @Override
    public void indexDocument(String id, String text, String customPrompt, Map<String, String> extraMetadata) {
        log.info("Indexando documento con id: {}", id);
        String prompt = (customPrompt != null && !customPrompt.isBlank()) ? customPrompt : DEFAULT_PROMPT;

        // Agregamos documentId por defecto
        Map<String, String> baseMetadata = new HashMap<>(extraMetadata);
        baseMetadata.putIfAbsent("documentId", id);

        List<TextSegment> segments = textChunkingService.chunk(text, baseMetadata);

        for (TextSegment segment : segments) {
            String promptFragment = prompt + "\n" + segment.text();
            Embedding embedding = embeddingGenerator.generateEmbedding(new TextSegment(promptFragment, segment.metadata()));

            String docName = id;
            if (segment.metadata() != null && segment.metadata().asMap().containsKey("chunkIndex")) {
                docName += "/fragment-" + segment.metadata().asMap().get("chunkIndex");
            }

            embeddingStorage.store(docName, embedding, segment.text(), segment.metadata());
            log.info("Fragmento almacenado con id: {}", docName);
        }

        log.info("Documento indexado correctamente: {}", id);
    }

    @Override
    public void index(List<TextSegment> segments) {
        log.info("Indexando lista de {} fragmentos manuales.", segments.size());
        for (TextSegment segment : segments) {
            String docId = Optional.ofNullable(segment.metadata().get("documentId"))
                    .map(Object::toString)
                    .orElse(UUID.randomUUID().toString());

            String prompt = DEFAULT_PROMPT + "\n" + segment.text();
            Embedding embedding = embeddingGenerator.generateEmbedding(new TextSegment(prompt, segment.metadata()));

            String chunkId = docId;
            if (segment.metadata() != null && segment.metadata().asMap().containsKey("chunkIndex")) {
                chunkId += "/fragment-" + segment.metadata().asMap().get("chunkIndex");
            }

            embeddingStorage.store(chunkId, embedding, segment.text(), segment.metadata());
            log.info("Fragmento almacenado con id: {}", chunkId);
        }
    }


}
