--liquibase formatted sql

--changeset flolec:001-alter-tattoo-artist-add-profile-fields
ALTER TABLE tattoo_artist
    ADD COLUMN artist_name       VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN slug              VARCHAR(30)  NOT NULL DEFAULT '',
    ADD COLUMN profile_photo_key VARCHAR(500);

ALTER TABLE tattoo_artist
    ADD CONSTRAINT uq_tattoo_artist_slug UNIQUE (slug);

ALTER TABLE tattoo_artist
    ALTER COLUMN artist_name DROP DEFAULT,
    ALTER COLUMN slug DROP DEFAULT;
--rollback ALTER TABLE tattoo_artist DROP COLUMN artist_name, DROP COLUMN slug, DROP COLUMN profile_photo_key;
