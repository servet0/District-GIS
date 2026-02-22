package com.geokarar.api.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
@Table(name = "districts")
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "district_name", nullable = false)
    private String districtName;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column
    private Integer population;

    @Column
    private Double income;

    @Column
    private Double education;

    @Column
    private Double infrastructure;

    @Column
    private Double employment;

    @Column(name = "development_index")
    private Double developmentIndex;

    @Column(columnDefinition = "geometry(MultiPolygon,4326)")
    private MultiPolygon geom;

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public Integer getPopulation() { return population; }
    public void setPopulation(Integer population) { this.population = population; }

    public Double getIncome() { return income; }
    public void setIncome(Double income) { this.income = income; }

    public Double getEducation() { return education; }
    public void setEducation(Double education) { this.education = education; }

    public Double getInfrastructure() { return infrastructure; }
    public void setInfrastructure(Double infrastructure) { this.infrastructure = infrastructure; }

    public Double getEmployment() { return employment; }
    public void setEmployment(Double employment) { this.employment = employment; }

    public Double getDevelopmentIndex() { return developmentIndex; }
    public void setDevelopmentIndex(Double developmentIndex) { this.developmentIndex = developmentIndex; }

    public MultiPolygon getGeom() { return geom; }
    public void setGeom(MultiPolygon geom) { this.geom = geom; }
}
