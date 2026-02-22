package com.geokarar.api.repository;

import com.geokarar.api.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    /**
     * Find districts whose development_index >= minScore.
     */
    @Query("SELECT d FROM District d WHERE d.developmentIndex >= :minScore")
    List<District> findByMinScore(@Param("minScore") Double minScore);

    /**
     * Spatial query: find districts within a radius (in metres) of a point.
     * Uses ST_DWithin with geography cast for accurate distance on SRID 4326.
     */
    @Query(value = """
        SELECT d.* FROM districts d
        WHERE ST_DWithin(
            d.geom::geography,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
            :radiusKm * 1000
        )
        """, nativeQuery = true)
    List<District> findWithinRadius(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm
    );
}
