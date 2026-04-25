--liquibase formatted sql

--changeset flolec:003-create-reference
CREATE TABLE reference
(
    id             UUID          NOT NULL,
    appointment_id UUID          NOT NULL,
    key            VARCHAR(1024) NOT NULL,
    comment        TEXT,
    CONSTRAINT pk_reference PRIMARY KEY (id),
    CONSTRAINT fk_reference_appointment FOREIGN KEY (appointment_id) REFERENCES appointment (id) ON DELETE CASCADE
);
CREATE INDEX idx_reference_appointment_id ON reference (appointment_id);
--rollback DROP TABLE reference;