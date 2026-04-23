--liquibase formatted sql

--changeset flolec:002-create-appointment
CREATE TABLE appointment
(
    id                 UUID         NOT NULL,
    artist_id          UUID         NOT NULL,
    client_id          UUID         NOT NULL,
    tattoo_description TEXT         NOT NULL,
    tattoo_placement   VARCHAR(255) NOT NULL,
    tattoo_size        VARCHAR(255) NOT NULL,
    first_tattoo       BOOLEAN      NOT NULL,
    cover_up           BOOLEAN      NOT NULL,
    submitted_at       TIMESTAMPTZ  NOT NULL,
    verified_at        TIMESTAMPTZ,
    new                BOOLEAN      NOT NULL DEFAULT true,
    CONSTRAINT pk_appointment PRIMARY KEY (id),
    CONSTRAINT fk_appointment_artist FOREIGN KEY (artist_id) REFERENCES tattoo_artist (id),
    CONSTRAINT fk_appointment_client FOREIGN KEY (client_id) REFERENCES tattoo_client (id)
);
CREATE INDEX idx_appointment_artist_id ON appointment (artist_id);
CREATE INDEX idx_appointment_client_id ON appointment (client_id);
--rollback DROP TABLE appointment;