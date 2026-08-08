--liquibase formatted sql

--changeset flolec:001-alter-tattoo-artist-add-profile-fields
ALTER TABLE tattoo_artist
    ADD COLUMN artist_name       VARCHAR(100) NOT NULL,
    ADD COLUMN slug              VARCHAR(30)  NOT NULL,
    ADD COLUMN profile_photo_key VARCHAR(500),
    ADD COLUMN city              VARCHAR(100) NOT NULL,
    ADD COLUMN country_code      VARCHAR(2)   NOT NULL,
    ADD COLUMN agenda_open       BOOLEAN      NOT NULL DEFAULT true;

CREATE UNIQUE INDEX uq_tattoo_artist_slug_activated ON tattoo_artist (slug) WHERE activated_at IS NOT NULL;
--rollback ALTER TABLE tattoo_artist DROP COLUMN artist_name, DROP COLUMN slug, DROP COLUMN profile_photo_key, DROP COLUMN city, DROP COLUMN country_code, DROP COLUMN agenda_open;
