import "./globals.css";

export const metadata = {
    title: "GeoKarar — GIS Decision Support System",
    description:
        "District-level GIS Decision Support Dashboard for Turkey — powered by PostGIS, Spring Boot & Leaflet.",
};

export default function RootLayout({ children }) {
    return (
        <html lang="tr">
            <head>
                <link
                    rel="stylesheet"
                    href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
                    integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
                    crossOrigin=""
                />
            </head>
            <body>{children}</body>
        </html>
    );
}
