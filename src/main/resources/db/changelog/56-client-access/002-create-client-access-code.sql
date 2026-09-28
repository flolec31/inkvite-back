--liquibase formatted sql

--changeset flolec:002-create-client-access-code
CREATE TABLE client_access_code
(
    client_id    UUID        NOT NULL,
    code         VARCHAR(6)  NOT NULL,
    attempts     INT         NOT NULL DEFAULT 0,
    expires_at   TIMESTAMPTZ NOT NULL,
    last_sent_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version      BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_client_access_code PRIMARY KEY (client_id),
    CONSTRAINT fk_client_access_code_tattoo_client FOREIGN KEY (client_id) REFERENCES tattoo_client (id)
);
--rollback DROP TABLE client_access_code;
