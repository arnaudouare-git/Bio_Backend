package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/** Reponse renvoyee pour une mesure. */
public class MesureResponse {

    private Long id;
    private Double temperature;
    private Double humidite;
    private LocalDateTime dateMesure;
    private Long capteurId;

    public MesureResponse(Long id, Double temperature, Double humidite,
                           LocalDateTime dateMesure, Long capteurId) {
        this.id = id;
        this.temperature = temperature;
        this.humidite = humidite;
        this.dateMesure = dateMesure;
        this.capteurId = capteurId;
    }

    public Long getId() {
        return id;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getHumidite() {
        return humidite;
    }

    public LocalDateTime getDateMesure() {
        return dateMesure;
    }

    public Long getCapteurId() {
        return capteurId;
    }
}
