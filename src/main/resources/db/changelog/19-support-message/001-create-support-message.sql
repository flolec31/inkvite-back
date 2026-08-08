--liquibase formatted sql

--changeset flolec:001-create-support-message
CREATE TABLE support_message
(
    id             UUID          NOT NULL,
    artist_id      UUID          NOT NULL,
    type           VARCHAR(20)   NOT NULL,
    message        VARCHAR(1500) NOT NULL,
    screenshot_key VARCHAR(1024),
    created_at     TIMESTAMPTZ   NOT NULL,
    CONSTRAINT pk_support_message PRIMARY KEY (id),
    CONSTRAINT fk_support_message_artist FOREIGN KEY (artist_id) REFERENCES tattoo_artist (id) ON DELETE CASCADE
);
CREATE INDEX idx_support_message_artist_id ON support_message (artist_id);
--rollback DROP TABLE support_message;
