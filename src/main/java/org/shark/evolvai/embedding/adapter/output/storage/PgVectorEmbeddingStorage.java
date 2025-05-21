package org.shark.evolvai.embedding.adapter.output.storage;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.shark.evolvai.embedding.port.output.EmbeddingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.util.*;

@Component
public class PgVectorEmbeddingStorage implements EmbeddingStorage {

    private static final Logger log = LoggerFactory.getLogger(PgVectorEmbeddingStorage.class);

    private final PgVectorEmbeddingStore embeddingStore;
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String tableName;

    public PgVectorEmbeddingStorage(
            @Value("${embedding.pgvector.host}") String host,
            @Value("${embedding.pgvector.port}") int port,
            @Value("${embedding.pgvector.database}") String database,
            @Value("${embedding.pgvector.user}") String user,
            @Value("${embedding.pgvector.password}") String password,
            @Value("${embedding.pgvector.tableName}") String tableName,
            @Value("${embedding.pgvector.dimensions}") int dimensions
    ) {
        try {
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
        } catch (Exception e) {
            log.error("Error inicializando PgVectorEmbeddingStore", e);
            throw e;
        }
    }

    @Override
    public void store(String id, Embedding embedding, String text) {
        String hash = hashText(text);
        String documentId = id + "_" + hash;

        // Verifica si ya existe ese document_id usando JDBC
        if (existsDocumentId(documentId)) {
            log.warn("Ya existe un documento con document_id={}. No se insertará nuevamente.", documentId);
            return;
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("documentName", id);
        meta.put("usuario", "desconocido");
        meta.put("timestamp", Instant.now().toEpochMilli());

        Metadata metadata = Metadata.from(meta);

        float[] original = embedding.vector();
        if (original.length > 1536) {
            throw new IllegalArgumentException("Embedding tiene más de 1536 dimensiones: " + original.length);
        }
        float[] padded = new float[1536];
        System.arraycopy(original, 0, padded, 0, original.length);

        // Inserta usando JDBC para poblar todas las columnas correctamente
        insertEmbedding(UUID.randomUUID(), padded, documentId, text, metadata.toMap());
        log.info("Embedding almacenado en PgVector con document_id={}, metadatos={}", documentId, metadata);
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
            ps.setObject(1, embeddingId);
            ps.setObject(2, embedding); // Puede requerir adaptación según el driver pgvector
            ps.setString(3, documentId);
            ps.setString(4, text);
            ps.setString(5, new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(metadata));
            ps.executeUpdate();
        } catch (Exception e) {
            log.error("Error insertando embedding", e);
        }
    }

    @Override
    public List<EmbeddingMatch<String>> findSimilar(Embedding embedding, int maxResults, double minScore) {
        // Mantén la lógica existente, pero revisa si hay métodos no obsoletos en PgVectorEmbeddingStore
        float[] original = embedding.vector();
        if (original.length > 1536) {
            throw new IllegalArgumentException("Embedding de búsqueda excede las 1536 dimensiones: " + original.length);
        }
        float[] padded = new float[1536];
        System.arraycopy(original, 0, padded, 0, original.length);

        Embedding paddedEmbedding = new Embedding(padded);

        return embeddingStore.findRelevant(paddedEmbedding, maxResults, minScore)
                .stream()
                .map(match -> new EmbeddingMatch<>(
                        match.score(),
                        match.embedded().metadata().get("documentName").toString(),
                        match.embedding(),
                        match.embedded().text()
                ))
                .toList();
    }

    @Override
    public void removeAll() {
        embeddingStore.removeAll();
        log.warn("Todos los embeddings han sido eliminados de PgVector.");
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