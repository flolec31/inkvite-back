--liquibase formatted sql

--changeset flolec:001-recreate-refresh-token-with-subject-discriminator
DROP TABLE IF EXISTS refresh_token;
CREATE TABLE refresh_token
(
    token        UUID        NOT NULL,
    subject_id   UUID        NOT NULL,
    subject_type VARCHAR(16) NOT NULL,
    expires_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_refresh_token PRIMARY KEY (token)
);
CREATE INDEX idx_refresh_token_subject ON refresh_token (subject_id, subject_type);
--rollback DROP TABLE refresh_token;
