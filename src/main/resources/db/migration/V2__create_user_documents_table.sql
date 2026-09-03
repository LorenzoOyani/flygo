CREATE TABLE user_documents
(
    id                   UUID PRIMARY KEY,
    user_id              UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    document_type        VARCHAR(50)   NOT NULL,
    cloudinary_url       VARCHAR(1000) NOT NULL,
    cloudinary_public_id VARCHAR(255)  NOT NULL,
    status               VARCHAR(20)   NOT NULL,
    rejection_reason     VARCHAR(500),
    created_at           TIMESTAMP     NOT NULL,
    updated_at           TIMESTAMP     NOT NULL,
    CONSTRAINT uk_user_document_type UNIQUE (user_id, document_type)
);

CREATE INDEX idx_user_documents_user_id ON user_documents (user_id);