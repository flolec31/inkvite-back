--liquibase formatted sql

--changeset flolec:001-create-password-reset-token
CREATE TABLE password_reset_token
(
    token            VARCHAR(255) NOT NULL,
    tattoo_artist_id UUID         NOT NULL,
    expires_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_password_reset_token PRIMARY KEY (token),
    CONSTRAINT fk_password_reset_token_tattoo_artist FOREIGN KEY (tattoo_artist_id) REFERENCES tattoo_artist (id)
);
CREATE INDEX idx_password_reset_token_tattoo_artist_id ON password_reset_token (tattoo_artist_id);
--rollback DROP TABLE password_reset_token;
