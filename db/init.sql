-- ============================================================
-- GeoKarar — Database Initialization
-- ============================================================

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Districts table
CREATE TABLE IF NOT EXISTS districts (
    id              BIGSERIAL PRIMARY KEY,
    district_name   TEXT NOT NULL,
    city_name       TEXT NOT NULL,
    population      INTEGER,
    income          DOUBLE PRECISION,
    education       DOUBLE PRECISION,
    infrastructure  DOUBLE PRECISION,
    employment      DOUBLE PRECISION,
    development_index DOUBLE PRECISION,
    geom            GEOMETRY(MultiPolygon, 4326)
);

-- Spatial index on geometry column
CREATE INDEX IF NOT EXISTS idx_districts_geom
    ON districts USING GIST (geom);

-- Index on development_index for fast filtering
CREATE INDEX IF NOT EXISTS idx_districts_dev_index
    ON districts (development_index);
