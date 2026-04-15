--liquibase formatted sql

--changeset flolec:002-create-verification-token
CREATE TABLE verification_token
(
    token            VARCHAR(255) NOT NULL,
    tattoo_artist_id UUID         NOT NULL,
    expires_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_verification_token PRIMARY KEY (token),
    CONSTRAINT fk_verification_token_tattoo_artist FOREIGN KEY (tattoo_artist_id) REFERENCES tattoo_artist (id)
);
--rollback DROP TABLE verification_token;