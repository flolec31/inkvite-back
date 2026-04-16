--liquibase formatted sql

--changeset flolec:001-create-tattoo-artist
CREATE TABLE tattoo_artist
(
    id            UUID                     NOT NULL,
    email         VARCHAR(255)             NOT NULL,
    password      VARCHAR(255)             NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    activated_at  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_tattoo_artist PRIMARY KEY (id),
    CONSTRAINT uq_tattoo_artist_email UNIQUE (email)
);
--rollback DROP TABLE tattoo_artist;
