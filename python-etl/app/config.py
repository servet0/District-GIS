"""Application configuration — reads from environment variables."""

import os


class Settings:
    DATABASE_URL: str = os.getenv(
        "DATABASE_URL",
        "postgresql://geokarar:geokarar_secret@localhost:5432/geokarar",
    )
    DATA_DIR: str = os.getenv("DATA_DIR", "/app/data")


settings = Settings()
