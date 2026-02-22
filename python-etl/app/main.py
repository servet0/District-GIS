"""FastAPI application — health check + ETL trigger endpoint."""

import logging
from fastapi import FastAPI, HTTPException

from app.etl import run_etl

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="GeoKarar ETL Service",
    description="Data processing pipeline for the GeoKarar Decision Support System",
    version="1.0.0",
)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/etl/run")
def trigger_etl():
    """Run the full ETL pipeline (CSV → normalize → PostGIS)."""
    try:
        rows = run_etl()
        return {"status": "success", "rows_processed": rows}
    except FileNotFoundError as exc:
        logger.error("CSV file not found: %s", exc)
        raise HTTPException(status_code=404, detail=str(exc))
    except Exception as exc:
        logger.exception("ETL failed")
        raise HTTPException(status_code=500, detail=str(exc))
