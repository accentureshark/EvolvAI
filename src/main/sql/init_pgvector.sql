-- Enable pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- Schemas for structured separation
CREATE SCHEMA IF NOT EXISTS embedding;
CREATE SCHEMA IF NOT EXISTS admin;

-- Main embeddings table
CREATE TABLE IF NOT EXISTS embedding.embeddings (
                                                    embedding_id       UUID PRIMARY KEY,
                                                    embedding          VECTOR(1536) NOT NULL,
                                                    text               TEXT NOT NULL,
                                                    metadata           JSONB,
                                                    uploader_user_id   UUID,
                                                    document_name      TEXT,
                                                    document_url       TEXT,
                                                    upload_timestamp   TIMESTAMP
);

CREATE INDEX IF NOT EXISTS embeddings_vector_idx
    ON embedding.embeddings USING ivfflat (embedding vector_l2_ops);
