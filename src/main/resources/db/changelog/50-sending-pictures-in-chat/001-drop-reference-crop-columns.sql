--liquibase formatted sql

--changeset flolec:001-drop-reference-crop-columns
ALTER TABLE reference
    DROP COLUMN crop_left,
    DROP COLUMN crop_top,
    DROP COLUMN crop_width,
    DROP COLUMN crop_height;
--rollback ALTER TABLE reference ADD COLUMN crop_left INTEGER NOT NULL DEFAULT 0, ADD COLUMN crop_top INTEGER NOT NULL DEFAULT 0, ADD COLUMN crop_width INTEGER NOT NULL DEFAULT 0, ADD COLUMN crop_height INTEGER NOT NULL DEFAULT 0;
