"""
ETL pipeline:
  1. Read CSV data
  2. Normalize features (min-max) via scikit-learn
  3. Compute development_index
  4. Upsert rows into PostGIS
"""

import os
import logging

import pandas as pd
from sklearn.preprocessing import MinMaxScaler
from sqlalchemy import create_engine, text
from shapely.geometry import MultiPolygon, Polygon
from geoalchemy2.shape import from_shape

from app.config import settings

logger = logging.getLogger(__name__)

# Weights for development index
WEIGHTS = {
    "income": 0.35,
    "education": 0.25,
    "infrastructure": 0.20,
    "employment": 0.20,
}

FEATURE_COLS = list(WEIGHTS.keys())


def _load_csv(path: str) -> pd.DataFrame:
    """Read the TÜİK-style CSV file."""
    df = pd.read_csv(path)
    required = {"district_name", "city_name", "population", "lat", "lng"} | set(FEATURE_COLS)
    missing = required - set(df.columns)
    if missing:
        raise ValueError(f"CSV is missing columns: {missing}")
    return df


def _normalize(df: pd.DataFrame) -> pd.DataFrame:
    """Min-max normalize the feature columns to [0, 100]."""
    scaler = MinMaxScaler(feature_range=(0, 100))
    df[FEATURE_COLS] = scaler.fit_transform(df[FEATURE_COLS])
    return df


def _compute_index(df: pd.DataFrame) -> pd.DataFrame:
    """Compute weighted development index."""
    df["development_index"] = sum(
        df[col] * weight for col, weight in WEIGHTS.items()
    )
    return df


def _make_point_geometry(lat: float, lng: float) -> str:
    """Create a tiny MultiPolygon around a point for demo purposes.

    In production this would be replaced by actual district boundary
    polygons from a shapefile.
    """
    offset = 0.05  # ~5 km bounding box
    poly = Polygon([
        (lng - offset, lat - offset),
        (lng + offset, lat - offset),
        (lng + offset, lat + offset),
        (lng - offset, lat + offset),
        (lng - offset, lat - offset),
    ])
    return from_shape(MultiPolygon([poly]), srid=4326)


def run_etl() -> int:
    """Execute the full ETL pipeline. Returns the number of rows upserted."""
    csv_path = os.path.join(settings.DATA_DIR, "sample_districts.csv")
    logger.info("Reading CSV from %s", csv_path)

    df = _load_csv(csv_path)
    df = _normalize(df)
    df = _compute_index(df)

    engine = create_engine(settings.DATABASE_URL)

    rows_written = 0
    with engine.begin() as conn:
        # Truncate to allow idempotent re-runs
        conn.execute(text("TRUNCATE TABLE districts RESTART IDENTITY"))

        for _, row in df.iterrows():
            geom = _make_point_geometry(row["lat"], row["lng"])
            conn.execute(
                text("""
                    INSERT INTO districts
                        (district_name, city_name, population,
                         income, education, infrastructure, employment,
                         development_index, geom)
                    VALUES
                        (:district_name, :city_name, :population,
                         :income, :education, :infrastructure, :employment,
                         :development_index, :geom)
                """),
                {
                    "district_name": row["district_name"],
                    "city_name": row["city_name"],
                    "population": int(row["population"]),
                    "income": float(row["income"]),
                    "education": float(row["education"]),
                    "infrastructure": float(row["infrastructure"]),
                    "employment": float(row["employment"]),
                    "development_index": float(row["development_index"]),
                    "geom": geom,
                },
            )
            rows_written += 1

    logger.info("Upserted %d rows into districts table", rows_written)
    return rows_written
