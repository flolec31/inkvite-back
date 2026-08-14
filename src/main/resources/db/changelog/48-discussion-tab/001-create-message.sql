--liquibase formatted sql

--changeset flolec:001-create-message
CREATE TABLE message
(
    id             UUID NOT NULL,
    appointment_id UUID NOT NULL,
    sender         VARCHAR(16) NOT NULL,
    content        VARCHAR(2000) NOT NULL,
    sent_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    read_at        TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_message PRIMARY KEY (id),
    CONSTRAINT fk_message_appointment FOREIGN KEY (appointment_id) REFERENCES appointment (id) ON DELETE CASCADE
);
CREATE INDEX idx_message_appointment_id_sent_at ON message (appointment_id, sent_at);
--rollback DROP TABLE message;
