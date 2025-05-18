CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS embeddings
(
    embedding_id UUID PRIMARY KEY,
    embedding    VECTOR(1536),
    text         TEXT,
    metadata     JSONB
);

CREATE INDEX IF NOT EXISTS embeddings_vector_idx ON embeddings USING ivfflat (embedding vector_l2_ops);