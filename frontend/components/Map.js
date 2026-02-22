"use client";

import { useEffect, useState, useCallback, useRef } from "react";
import { MapContainer, TileLayer, GeoJSON, useMap } from "react-leaflet";
import L from "leaflet";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

// ── Colour scale for development_index ──────────────────────
const COLOUR_STOPS = [
    { min: 0, max: 20, color: "#dc2626", label: "0 – 20  (Very Low)" },
    { min: 20, max: 40, color: "#f97316", label: "20 – 40 (Low)" },
    { min: 40, max: 60, color: "#eab308", label: "40 – 60 (Medium)" },
    { min: 60, max: 80, color: "#22c55e", label: "60 – 80 (High)" },
    { min: 80, max: 100, color: "#06b6d4", label: "80 – 100 (Very High)" },
];

function getColor(score) {
    for (const s of COLOUR_STOPS) {
        if (score < s.max) return s.color;
    }
    return COLOUR_STOPS[COLOUR_STOPS.length - 1].color;
}

// ── Auto-fit bounds when data changes ───────────────────────
function FitBounds({ geoData }) {
    const map = useMap();
    useEffect(() => {
        if (geoData && geoData.features && geoData.features.length > 0) {
            const layer = L.geoJSON(geoData);
            const bounds = layer.getBounds();
            if (bounds.isValid()) {
                map.fitBounds(bounds, { padding: [40, 40] });
            }
        }
    }, [geoData, map]);
    return null;
}

// ── Main Map Component ──────────────────────────────────────
export default function GeoKararMap() {
    const [geoData, setGeoData] = useState(null);
    const [minScore, setMinScore] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const geoJsonRef = useRef(null);

    const fetchDistricts = useCallback(async (score) => {
        setLoading(true);
        setError(null);
        try {
            const url =
                score > 0
                    ? `${API_URL}/districts?minScore=${score}`
                    : `${API_URL}/districts`;
            const res = await fetch(url);
            if (!res.ok) throw new Error(`API responded with ${res.status}`);
            const data = await res.json();
            setGeoData(data);
        } catch (err) {
            console.error("Failed to fetch districts:", err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchDistricts(minScore);
    }, [minScore, fetchDistricts]);

    const handleSlider = (e) => {
        setMinScore(Number(e.target.value));
    };

    // Choropleth style
    const style = (feature) => {
        const score = feature.properties.developmentIndex || 0;
        return {
            fillColor: getColor(score),
            weight: 2,
            opacity: 1,
            color: "#1e293b",
            fillOpacity: 0.75,
        };
    };

    // Popup on each feature
    const onEachFeature = (feature, layer) => {
        const p = feature.properties;
        const score = p.developmentIndex?.toFixed(1) ?? "N/A";
        const color = getColor(p.developmentIndex || 0);

        layer.bindPopup(`
      <div class="popup-content">
        <h3>${p.districtName}</h3>
        <div class="city">${p.cityName}</div>
        <div class="popup-stats">
          <div class="popup-stat">
            <span class="stat-label">Population</span>
            <span class="stat-value">${(p.population || 0).toLocaleString("tr-TR")}</span>
          </div>
          <div class="popup-stat">
            <span class="stat-label">Income</span>
            <span class="stat-value">${p.income?.toFixed(1) ?? "-"}</span>
          </div>
          <div class="popup-stat">
            <span class="stat-label">Education</span>
            <span class="stat-value">${p.education?.toFixed(1) ?? "-"}</span>
          </div>
          <div class="popup-stat">
            <span class="stat-label">Infrastructure</span>
            <span class="stat-value">${p.infrastructure?.toFixed(1) ?? "-"}</span>
          </div>
          <div class="popup-stat">
            <span class="stat-label">Employment</span>
            <span class="stat-value">${p.employment?.toFixed(1) ?? "-"}</span>
          </div>
        </div>
        <div class="popup-index">
          <span class="stat-label">Development Index</span>
          <span class="index-value" style="color:${color}">${score}</span>
        </div>
      </div>
    `);

        layer.on({
            mouseover: (e) => {
                e.target.setStyle({ weight: 3, fillOpacity: 0.9 });
                e.target.bringToFront();
            },
            mouseout: (e) => {
                if (geoJsonRef.current) {
                    geoJsonRef.current.resetStyle(e.target);
                }
            },
        });
    };

    const featureCount = geoData?.features?.length ?? 0;

    return (
        <div className="app-container">
            {/* ── Header ──────────────────────────────────────────── */}
            <header className="header">
                <div className="header-brand">
                    <h1>GeoKarar</h1>
                    <span className="badge">Decision Support</span>
                </div>

                <div className="controls-panel">
                    <div className="slider-group">
                        <label htmlFor="minScore">Min Score</label>
                        <input
                            id="minScore"
                            type="range"
                            min={0}
                            max={100}
                            step={5}
                            value={minScore}
                            onChange={handleSlider}
                        />
                        <span className="score-badge">{minScore}</span>
                    </div>
                    <span className="district-count">
                        {featureCount} district{featureCount !== 1 ? "s" : ""}
                    </span>
                </div>
            </header>

            {/* ── Map ─────────────────────────────────────────────── */}
            <div className="map-wrapper">
                {loading && (
                    <div className="loading-overlay">
                        <div className="spinner" />
                    </div>
                )}

                {error && (
                    <div className="loading-overlay">
                        <div style={{ textAlign: "center" }}>
                            <p style={{ color: "#ef4444", fontWeight: 600 }}>
                                Failed to load data
                            </p>
                            <p style={{ color: "#94a3b8", fontSize: "0.85rem", marginTop: 6 }}>
                                {error}
                            </p>
                        </div>
                    </div>
                )}

                <MapContainer
                    center={[39.0, 35.0]}
                    zoom={6}
                    style={{ height: "100%", width: "100%" }}
                    zoomControl={true}
                >
                    <TileLayer
                        attribution='&copy; <a href="https://carto.com/">CARTO</a>'
                        url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
                    />

                    {geoData && (
                        <>
                            <GeoJSON
                                key={JSON.stringify(geoData)}
                                data={geoData}
                                style={style}
                                onEachFeature={onEachFeature}
                                ref={geoJsonRef}
                            />
                            <FitBounds geoData={geoData} />
                        </>
                    )}
                </MapContainer>

                {/* ── Legend ───────────────────────────────────────── */}
                <div className="legend">
                    <h4>Development Index</h4>
                    {COLOUR_STOPS.map((s) => (
                        <div key={s.label} className="legend-item">
                            <span
                                className="legend-color"
                                style={{ background: s.color }}
                            />
                            <span>{s.label}</span>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
