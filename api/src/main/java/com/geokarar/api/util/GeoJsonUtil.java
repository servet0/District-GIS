package com.geokarar.api.util;

import com.geokarar.api.entity.District;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.*;

/**
 * Converts District entities into GeoJSON FeatureCollection format.
 */
public final class GeoJsonUtil {

    private GeoJsonUtil() {}

    /**
     * Build a GeoJSON FeatureCollection from a list of districts.
     */
    public static Map<String, Object> toFeatureCollection(List<District> districts) {
        Map<String, Object> fc = new LinkedHashMap<>();
        fc.put("type", "FeatureCollection");

        List<Map<String, Object>> features = new ArrayList<>();
        for (District d : districts) {
            features.add(toFeature(d));
        }
        fc.put("features", features);
        return fc;
    }

    /**
     * Build a single GeoJSON Feature from a district.
     */
    public static Map<String, Object> toFeature(District d) {
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("id", d.getId());

        // Properties
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("districtName", d.getDistrictName());
        props.put("cityName", d.getCityName());
        props.put("population", d.getPopulation());
        props.put("income", d.getIncome());
        props.put("education", d.getEducation());
        props.put("infrastructure", d.getInfrastructure());
        props.put("employment", d.getEmployment());
        props.put("developmentIndex", d.getDevelopmentIndex());
        feature.put("properties", props);

        // Geometry
        feature.put("geometry", geometryToMap(d.getGeom()));
        return feature;
    }

    private static Map<String, Object> geometryToMap(Geometry geom) {
        Map<String, Object> geoJson = new LinkedHashMap<>();
        if (geom == null) {
            geoJson.put("type", "MultiPolygon");
            geoJson.put("coordinates", Collections.emptyList());
            return geoJson;
        }

        geoJson.put("type", geom.getGeometryType());

        // Build coordinates array for MultiPolygon
        List<List<List<List<Double>>>> multiCoords = new ArrayList<>();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Geometry polygon = geom.getGeometryN(i);
            List<List<List<Double>>> polyCoords = new ArrayList<>();

            // Exterior ring
            Coordinate[] coords = polygon.getCoordinates();
            List<List<Double>> ring = new ArrayList<>();
            for (Coordinate c : coords) {
                ring.add(Arrays.asList(c.x, c.y));
            }
            polyCoords.add(ring);
            multiCoords.add(polyCoords);
        }
        geoJson.put("coordinates", multiCoords);
        return geoJson;
    }
}
