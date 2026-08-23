--liquibase formatted sql

--changeset flolec:001-add-appointment-color
ALTER TABLE appointment
    ADD COLUMN color BOOLEAN NOT NULL DEFAULT false;
--rollback ALTER TABLE appointment DROP COLUMN color;
