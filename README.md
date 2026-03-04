# 🌍 GeoKarar — District-level GIS Decision Support System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-black?logo=next.js)](https://nextjs.org/)
[![PostGIS](https://img.shields.io/badge/PostGIS-3.4-blue?logo=postgresql)](https://postgis.net/)
[![Python](https://img.shields.io/badge/Python-3.11-yellow?logo=python)](https://python.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://docs.docker.com/compose/)

**GeoKarar** is a hybrid GIS-based Decision Support System (DSS) designed for district-level socioeconomic analysis of Turkey. It computes a weighted **Development Index** for each district based on income, education, infrastructure, and employment metrics, then visualises the results on an interactive choropleth map.

---

## 📐 Architecture Overview

```
┌─────────────────┐       ┌──────────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│   Python ETL    │       │   PostgreSQL 16 +    │       │   Spring Boot    │       │   Next.js 14 +   │
│   (FastAPI)     │──────▶│   PostGIS 3.4        │◀──────│   REST API       │◀──────│   Leaflet Map    │
│   Port: 8000    │ SQL   │   Port: 5432         │  JPA  │   Port: 8080     │ HTTP  │   Port: 3000     │
└─────────────────┘       └──────────────────────┘       └──────────────────┘       └──────────────────┘
       ▲                          ▲                              │                          │
       │                          │                              │                          │
   CSV Data               BIGSERIAL + GIST              GeoJSON Response            Choropleth +
   Ingestion              Spatial Index                 FeatureCollection           Popup + Slider
```

### Data Flow

1. **Python ETL** reads TÜİK-style CSV data, normalises features via MinMaxScaler, computes the development index, and inserts rows with geometry into PostGIS.
2. **PostgreSQL + PostGIS** stores district data with `MultiPolygon` geometry (SRID 4326) and provides spatial query capabilities via GIST index.
3. **Spring Boot API** connects to PostGIS via Hibernate Spatial, exposes RESTful endpoints returning **GeoJSON** format.
4. **Next.js Frontend** fetches GeoJSON from the API and renders an interactive Leaflet choropleth map with filtering, popups, and a colour-coded legend.

---

## 🧮 Development Index Formula

Each district receives a **Development Index** score (0–100) computed from four normalised indicators:

```
development_index = 0.35 × income
                  + 0.25 × education
                  + 0.20 × infrastructure
                  + 0.20 × employment
```

All raw values are first normalised to `[0, 100]` using **sklearn.preprocessing.MinMaxScaler** to ensure fair comparison across districts with differing scales.

---

## 🗄️ Database Schema

```sql
CREATE TABLE districts (
    id                BIGSERIAL PRIMARY KEY,
    district_name     TEXT NOT NULL,
    city_name         TEXT NOT NULL,
    population        INTEGER,
    income            DOUBLE PRECISION,
    education         DOUBLE PRECISION,
    infrastructure    DOUBLE PRECISION,
    employment        DOUBLE PRECISION,
    development_index DOUBLE PRECISION,
    geom              GEOMETRY(MultiPolygon, 4326)
);

-- Spatial index for geometry queries (ST_DWithin, ST_Contains, etc.)
CREATE INDEX idx_districts_geom ON districts USING GIST (geom);

-- B-tree index for fast score filtering
CREATE INDEX idx_districts_dev_index ON districts (development_index);
```

---

## 📡 REST API Endpoints

All endpoints are served by Spring Boot on port `8080` and return **GeoJSON** (RFC 7946) format.

| Method | Endpoint | Description | Example |
|--------|----------|-------------|---------|
| `GET` | `/api/districts` | All districts as GeoJSON FeatureCollection | `http://localhost:8080/api/districts` |
| `GET` | `/api/districts?minScore=70` | Filter districts by minimum development index | `http://localhost:8080/api/districts?minScore=70` |
| `GET` | `/api/districts/{id}` | Single district as GeoJSON Feature | `http://localhost:8080/api/districts/1` |
| `GET` | `/api/districts/within?lat=39.9&lng=32.8&radius=50` | Spatial query: districts within radius (km) | Uses `ST_DWithin` with geography cast |

### Example Response

```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "id": 1,
      "properties": {
        "districtName": "Çankaya",
        "cityName": "Ankara",
        "population": 944609,
        "income": 64.29,
        "education": 81.82,
        "infrastructure": 80.00,
        "employment": 65.71,
        "developmentIndex": 72.50
      },
      "geometry": {
        "type": "MultiPolygon",
        "coordinates": [[[[32.81, 39.85], [32.91, 39.85], ...]]]
      }
    }
  ]
}
```

---

## 🗂️ Project Structure

```
District-GIS/
│
├── docker-compose.yml              # Orchestrates all 4 services
├── .env.example                    # Template for environment variables
├── .gitignore                      # Excludes .env, target/, node_modules/, etc.
├── README.md                       # This file
│
├── db/
│   └── init.sql                    # PostGIS schema, indexes, extensions
│
├── python-etl/                     # ── Data Processing Layer ──
│   ├── Dockerfile
│   ├── requirements.txt            # FastAPI, Pandas, GeoPandas, Scikit-learn
│   ├── app/
│   │   ├── __init__.py
│   │   ├── main.py                 # FastAPI app: /health, POST /etl/run
│   │   ├── config.py               # Environment-based settings
│   │   ├── etl.py                  # ETL pipeline: CSV → normalize → index → PostGIS
│   │   └── models.py               # SQLAlchemy + GeoAlchemy2 model
│   └── data/
│       └── sample_districts.csv    # Sample TÜİK-style data (10 districts)
│
├── api/                            # ── Spring Boot API Layer ──
│   ├── Dockerfile                  # Multi-stage: Maven build → JRE runtime
│   ├── pom.xml                     # Spring Boot 3.2, Hibernate Spatial, PostgreSQL
│   └── src/main/
│       ├── java/com/geokarar/api/
│       │   ├── GeoKararApplication.java          # Entry point
│       │   ├── entity/District.java              # JPA entity with JTS MultiPolygon
│       │   ├── repository/DistrictRepository.java # Spring Data + native spatial queries
│       │   ├── service/DistrictService.java       # Business logic
│       │   ├── controller/DistrictController.java # REST endpoints (GeoJSON)
│       │   └── util/GeoJsonUtil.java              # JTS → GeoJSON converter
│       └── resources/
│           └── application.yml     # Datasource config, Hibernate settings
│
└── frontend/                       # ── Next.js Frontend ──
    ├── Dockerfile
    ├── package.json                # Next.js 14, Leaflet, React-Leaflet
    ├── next.config.js              # API URL configuration
    ├── app/
    │   ├── layout.js               # Root layout + Leaflet CSS
    │   ├── page.js                 # Main page (dynamic import for SSR bypass)
    │   └── globals.css             # Dark-mode design system
    └── components/
        └── Map.js                  # Leaflet choropleth map component
```

---

## 🚀 Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)

### Step 1 — Clone and Configure

```bash
git clone https://github.com/servet0/District-GIS.git 
cd District-GIS
cp .env.example .env        # Create your local .env and update credentials
docker compose up -d --build
```

Wait until all 4 containers are healthy (~ 2-3 minutes on first build):

```bash
docker ps --filter "name=geokarar" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Expected output:

```
NAMES                STATUS                    PORTS
geokarar-frontend    Up About a minute         0.0.0.0:3000->3000/tcp
geokarar-api         Up About a minute         0.0.0.0:8080->8080/tcp
geokarar-etl         Up About a minute         0.0.0.0:8000->8000/tcp
geokarar-db          Up About a minute (healthy) 0.0.0.0:5432->5432/tcp
```

### Step 2 — Load Sample Data

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8000/etl/run" -Method Post
```

**Git Bash / WSL / Mac / Linux:**
```bash
curl -X POST http://localhost:8000/etl/run
```

Expected response:
```json
{ "status": "success", "rows_processed": 10 }
```

### Step 3 — Open the Dashboard

Navigate to **http://localhost:3000** in your browser.

You should see:
- 🗺️ A dark-themed Leaflet map centred on Turkey
- 🟩🟧🟥 Coloured polygons for each district (choropleth)
- 🎚️ A **Min Score** slider to filter districts by development index
- 📊 Click any district polygon to see a popup with all metrics

### Step 4 — Test API Endpoints

**PowerShell:**
```powershell
# All districts
Invoke-RestMethod -Uri "http://localhost:8080/api/districts"

# Filtered by score
Invoke-RestMethod -Uri "http://localhost:8080/api/districts?minScore=50"

# Single district
Invoke-RestMethod -Uri "http://localhost:8080/api/districts/1"

# Spatial query (50km around Ankara)
Invoke-RestMethod -Uri "http://localhost:8080/api/districts/within?lat=39.9&lng=32.8&radius=50"
```

### Stopping the Stack

```bash
docker compose down        # Stop and remove containers
docker compose down -v     # Also removes the database volume (full reset)
```

---

## ⚙️ Environment Variables

Copy `.env.example` to `.env` and fill in your credentials:

```bash
cp .env.example .env
```

| Variable | Description | Used By |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | DB |
| `POSTGRES_USER` | Database username | DB, ETL, API |
| `POSTGRES_PASSWORD` | Database password | DB, ETL, API |
| `SPRING_DATASOURCE_URL` | JDBC connection string | API |
| `DATABASE_URL` | Python connection string | ETL |
| `NEXT_PUBLIC_API_URL` | API base URL for frontend | Frontend |

> ⚠️ **`.env` is in `.gitignore`** — credentials are never committed. Only `.env.example` (without real passwords) is tracked.

---

## 🛠️ Technology Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Database | PostgreSQL + PostGIS | 16 + 3.4 | Spatial data storage, GIST indexing |
| ETL | Python + FastAPI | 3.11 | CSV import, normalisation, index computation |
| ETL | Pandas + Scikit-learn | 2.2 + 1.4 | Data manipulation, MinMaxScaler |
| ETL | GeoAlchemy2 + Shapely | 0.14 + 2.0 | Geometry creation (WKT) |
| API | Java + Spring Boot | 17 + 3.2 | RESTful API, GeoJSON serialisation |
| API | Hibernate Spatial | 6.4 | JPA with JTS geometry types |
| Frontend | Next.js + React | 14 + 18 | Server-side rendering, component UI |
| Frontend | Leaflet + React-Leaflet | 1.9 + 4.2 | Interactive map, choropleth, popups |
| Infra | Docker + Compose | 28.x | Container orchestration |

---

## 📄 License

MIT
