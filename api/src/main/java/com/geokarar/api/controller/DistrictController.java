package com.geokarar.api.controller;

import com.geokarar.api.entity.District;
import com.geokarar.api.service.DistrictService;
import com.geokarar.api.util.GeoJsonUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/districts")
@CrossOrigin(origins = "*")
public class DistrictController {

    private final DistrictService service;

    public DistrictController(DistrictService service) {
        this.service = service;
    }

    /**
     * GET /api/districts
     * GET /api/districts?minScore=70
     * Returns GeoJSON FeatureCollection.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDistricts(
            @RequestParam(required = false) Double minScore) {

        List<District> districts = service.findAll(minScore);
        return ResponseEntity.ok(GeoJsonUtil.toFeatureCollection(districts));
    }

    /**
     * GET /api/districts/{id}
     * Returns a single GeoJSON Feature.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDistrictById(@PathVariable Long id) {
        return service.findById(id)
                .map(d -> ResponseEntity.ok(GeoJsonUtil.toFeature(d)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/districts/within?lat=39.9&lng=32.8&radius=50
     * Spatial query — returns GeoJSON FeatureCollection.
     * Radius is in kilometres.
     */
    @GetMapping("/within")
    public ResponseEntity<Map<String, Object>> getDistrictsWithin(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radius) {

        List<District> districts = service.findWithinRadius(lat, lng, radius);
        return ResponseEntity.ok(GeoJsonUtil.toFeatureCollection(districts));
    }
}
