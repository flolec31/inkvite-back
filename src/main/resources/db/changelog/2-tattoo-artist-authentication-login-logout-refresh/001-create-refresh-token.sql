--liquibase formatted sql

--changeset flolec:001-create-refresh-token
CREATE TABLE refresh_token
(
    token            UUID        NOT NULL,
    tattoo_artist_id UUID        NOT NULL,
    expires_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_refresh_token PRIMARY KEY (token),
    CONSTRAINT fk_refresh_token_tattoo_artist FOREIGN KEY (tattoo_artist_id) REFERENCES tattoo_artist (id) ON DELETE CASCADE
);
--rollback DROP TABLE refresh_token;
