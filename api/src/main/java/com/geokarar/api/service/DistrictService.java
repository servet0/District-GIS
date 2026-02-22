package com.geokarar.api.service;

import com.geokarar.api.entity.District;
import com.geokarar.api.repository.DistrictRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DistrictService {

    private final DistrictRepository repository;

    public DistrictService(DistrictRepository repository) {
        this.repository = repository;
    }

    public List<District> findAll(Double minScore) {
        if (minScore != null) {
            return repository.findByMinScore(minScore);
        }
        return repository.findAll();
    }

    public Optional<District> findById(Long id) {
        return repository.findById(id);
    }

    public List<District> findWithinRadius(double lat, double lng, double radiusKm) {
        return repository.findWithinRadius(lat, lng, radiusKm);
    }
}
