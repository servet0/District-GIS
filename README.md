# 🌍 GeoKarar — District-level GIS Decision Support System

District-level GIS Decision Support Dashboard for Turkey.  
**Hybrid architecture**: Python ETL → PostGIS → Spring Boot API → Next.js + Leaflet

---

## Architecture

```
┌──────────────┐     ┌──────────────────┐     ┌───────────────┐     ┌─────────────────┐
│  Python ETL  │────▶│  PostgreSQL +    │◀────│  Spring Boot  │◀────│  Next.js +      │
│  (FastAPI)   │     │  PostGIS         │     │  REST API     │     │  Leaflet Map    │
│  :8000       │     │  :5432           │     │  :8080        │     │  :3000          │
└──────────────┘     └──────────────────┘     └───────────────┘     └─────────────────┘
```

## Quick Start

### Prerequisites
- Docker & Docker Compose

### 1. Start all services

```bash
docker compose up --build
```

### 2. Load sample data

```bash
curl -X POST http://localhost:8000/etl/run
```

### 3. Open the dashboard

Navigate to **http://localhost:3000**

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/districts` | All districts (GeoJSON) |
| `GET` | `/api/districts?minScore=70` | Filter by min development index |
| `GET` | `/api/districts/{id}` | Single district (GeoJSON Feature) |
| `GET` | `/api/districts/within?lat=39.9&lng=32.8&radius=50` | Spatial query by radius (km) |

---

## Development Index Formula

```
development_index =
    0.35 × income +
    0.25 × education +
    0.20 × infrastructure +
    0.20 × employment
```

Features are normalised to [0, 100] via min-max scaling before computation.

---

## Project Structure

```
District-GIS/
├── docker-compose.yml          # Orchestrates all services
├── .env                        # Environment variables
├── db/
│   └── init.sql                # PostGIS schema + indexes
├── python-etl/                 # Data processing service
│   ├── Dockerfile
│   ├── requirements.txt
│   ├── app/
│   │   ├── main.py             # FastAPI endpoints
│   │   ├── config.py           # Environment config
│   │   ├── etl.py              # ETL pipeline
│   │   └── models.py           # SQLAlchemy models
│   └── data/
│       └── sample_districts.csv
├── api/                        # Spring Boot REST API
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/geokarar/api/
│       │   ├── GeoKararApplication.java
│       │   ├── entity/District.java
│       │   ├── repository/DistrictRepository.java
│       │   ├── service/DistrictService.java
│       │   ├── controller/DistrictController.java
│       │   └── util/GeoJsonUtil.java
│       └── resources/application.yml
└── frontend/                   # Next.js dashboard
    ├── Dockerfile
    ├── package.json
    ├── next.config.js
    ├── app/
    │   ├── layout.js
    │   ├── page.js
    │   └── globals.css
    └── components/
        └── Map.js              # Leaflet choropleth map
```

---

## Services

| Service | Port | Technology |
|---------|------|-----------|
| Database | 5432 | PostgreSQL 16 + PostGIS 3.4 |
| ETL | 8000 | Python 3.11 + FastAPI |
| API | 8080 | Java 17 + Spring Boot 3.2 |
| Frontend | 3000 | Next.js 14 + Leaflet |

---

## Environment Variables

All variables are defined in `.env` and loaded by Docker Compose:

| Variable | Default | Used By |
|----------|---------|---------|
| `POSTGRES_DB` | `geokarar` | DB, Python |
| `POSTGRES_USER` | `geokarar` | DB, Python, API |
| `POSTGRES_PASSWORD` | `geokarar_secret` | DB, Python, API |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/geokarar` | API |
| `DATABASE_URL` | `postgresql://geokarar:...@db:5432/geokarar` | Python |
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080/api` | Frontend |

---

## License

MIT
