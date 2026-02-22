"use client";

import dynamic from "next/dynamic";

// Leaflet must be loaded client-side only (no SSR)
const GeoKararMap = dynamic(() => import("../components/Map"), { ssr: false });

export default function HomePage() {
    return <GeoKararMap />;
}
