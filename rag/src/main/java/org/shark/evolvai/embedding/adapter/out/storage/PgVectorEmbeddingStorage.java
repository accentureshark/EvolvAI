package org.shark.evolvai.embedding.adapter.out.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component("pgVectorEmbeddingStorage")
@ConditionalOnProperty(name = "embedding.storage.type", havingValue = "pgVector")
public class PgVectorEmbeddingStorage implements EmbeddingStorage {

    private static final Logger log = LoggerFactory.getLogger(PgVectorEmbeddingStorage.class);

    private final PgVectorEmbeddingStore embeddingStore;
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String tableName;
    private final int targetDimension;

    public PgVectorEmbeddingStorage(
            @Value("${embedding.pgvector.host}") String host,
            @Value("${embedding.pgvector.port}") int port,
            @Value("${embedding.pgvector.database}") String database,
            @Value("${embedding.pgvector.user}") String user,
            @Value("${embedding.pgvector.password}") String password,
            @Value("${embedding.pgvector.tableName}") String tableName,
            @Value("${embedding.pgvector.dimensions}") int dimensions
    ) {
        this.embeddingStore = PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(user)
                .password(password)
                .table(tableName)
                .dimension(dimensions)
                .build();

        this.jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        this.dbUser = user;
        this.dbPassword = password;
        this.tableName = tableName;
        this.targetDimension = dimensions;
    }

    @Override
    public void store(String id, Embedding embedding, String text) {
        store(id, embedding, text, null);
    }

    @Override
    public void store(String id, Embedding embedding, String text, Metadata metadata) {
        String hash = hashText(text);
        String documentId = id + "/" + hash;

        if (existsDocumentId(documentId)) {
            log.warn("Ya existe un documento con document_id={}. No se insertará nuevamente.", documentId);
            return;
        }

        if (metadata == null) {
            Map<String, Object> defaultMeta = new HashMap<>();
            defaultMeta.put("documentName", id);
            defaultMeta.put("usuario", "desconocido");
            defaultMeta.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
            metadata = Metadata.from(defaultMeta);
        }

        float[] padded = padToDimension(embedding.vector(), targetDimension);

        Map<String, Object> metaMap = metadata.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Object) e.getValue()
                ));

        insertEmbedding(UUID.randomUUID(), padded, documentId, text, metaMap);
        log.info("Embedding almacenado en PgVector con document_id={}, dimensiones={}, metadatos={}", documentId, padded.length, metadata);
    }

    private boolean existsDocumentId(String documentId) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE document_id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Error verificando existencia de document_id", e);
            return false;
        }
    }

    private void insertEmbedding(UUID embeddingId, float[] embedding, String documentId, String text, Map<String, Object> metadata) {
        String sql = "INSERT INTO " + tableName + " (embedding_id, embedding, document_id, text, metadata) VALUES (?, ?, ?, ?, ?::jsonb)";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Float[] floatObjects = new Float[embedding.length];
            for (int i = 0; i < embedding.length; i++) {
                floatObjects[i] = embedding[i];
            }
            Array pgVector = conn.createArrayOf("float4", floatObjects);

            ps.setObject(1, embeddingId);
            ps.setArray(2, pgVector);
            ps.setString(3, documentId);
            ps.setString(4, text);

            String json = new ObjectMapper().writeValueAsString(metadata);
            ps.setString(5, json);

            ps.executeUpdate();
        } catch (Exception e) {
            log.error("Error insertando embedding", e);
        }
    }

    @Override
    public List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore) {
        float[] padded = padToDimension(embedding.vector(), targetDimension);
        Embedding paddedEmbedding = new Embedding(padded);

        return embeddingStore.findRelevant(paddedEmbedding, maxResults, minScore)
                .stream()
                .map(match -> {
                    String documentName = Optional.ofNullable(match.embedded().metadata().get("documentId"))
                            .map(Object::toString)
                            .orElse("desconocido");
                    return new EmbeddingMatch<>(
                            match.score(),
                            documentName,
                            match.embedding(),
                            match.embedded().text()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore, Metadata filter) {
        float[] padded = padToDimension(embedding.vector(), targetDimension);
        Embedding paddedEmbedding = new Embedding(padded);

        Map<String, Object> filterMap = filter.asMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Object) e.getValue()
                ));

        return embeddingStore.findRelevant(paddedEmbedding, maxResults, minScore).stream()
                .filter(match -> {
                    Map<String, Object> meta = match.embedded().metadata().asMap().entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> (Object) e.getValue()
                            ));
                    return filterMap.entrySet().stream()
                            .allMatch(e -> e.getValue().toString().equals(
                                    Optional.ofNullable(meta.get(e.getKey())).map(Object::toString).orElse(null)));
                })
                .map(match -> {
                    String documentName = Optional.ofNullable(match.embedded().metadata().get("documentId"))
                            .map(Object::toString)
                            .orElse("desconocido");
                    return new EmbeddingMatch<>(
                            match.score(),
                            documentName,
                            match.embedding(),
                            match.embedded().text()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findAllDocumentIds() {
        String sql = "SELECT DISTINCT split_part(document_id, '/', 1) FROM " + tableName;
        List<String> ids = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
        } catch (SQLException e) {
            log.error("Error obteniendo document_id distintos", e);
        }
        return ids;
    }

    @Override
    public void removeAll() {
        embeddingStore.removeAll();
        log.warn("Todos los embeddings han sido eliminados de PgVector.");
    }

    private float[] padToDimension(float[] input, int dim) {
        float[] result = new float[dim];
        if (input.length > dim) {
            System.arraycopy(input, 0, result, 0, dim);
            log.warn("Truncando embedding de {} a {} dimensiones", input.length, dim);
        } else {
            System.arraycopy(input, 0, result, 0, input.length);
        }
        return result;
    }

    private String hashText(String text) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo calcular el hash SHA-256", e);
        }
    }
}
