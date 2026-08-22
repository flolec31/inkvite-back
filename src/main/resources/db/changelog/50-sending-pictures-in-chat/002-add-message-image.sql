--liquibase formatted sql

--changeset flolec:002-add-message-image
ALTER TABLE message ALTER COLUMN content DROP NOT NULL;
ALTER TABLE message ADD COLUMN image_key VARCHAR(1024);
--rollback ALTER TABLE message DROP COLUMN image_key;
--rollback ALTER TABLE message ALTER COLUMN content SET NOT NULL;
