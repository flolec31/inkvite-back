--liquibase formatted sql

--changeset flolec:001-create-tattoo-client
CREATE TABLE tattoo_client
(
    id         UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tattoo_client PRIMARY KEY (id),
    CONSTRAINT uq_tattoo_client_email UNIQUE (email)
);
--rollback DROP TABLE tattoo_client;