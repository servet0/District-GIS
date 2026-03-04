"""Application configuration — reads from environment variables."""

import os


class Settings:
    DATABASE_URL: str = os.environ["DATABASE_URL"]
    DATA_DIR: str = os.getenv("DATA_DIR", "/app/data")


settings = Settings()
