--liquibase formatted sql

--changeset flolec:002-add-appointment-style
ALTER TABLE appointment
    ADD COLUMN style VARCHAR(30) NOT NULL DEFAULT 'OTHER_UNKNOWN';
--rollback ALTER TABLE appointment DROP COLUMN style;
