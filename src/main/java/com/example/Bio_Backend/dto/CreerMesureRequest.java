package com.example.bio_backend.dto;

/**
 * Corps de la requete POST /api/mesures. Dans la vraie vie, c'est le
 * capteur physique (Arduino/ESP32) qui enverrait ceci automatiquement ;
 * ici on simule cet envoi via Postman.
 */
public class CreerMesureRequest {

    private Long capteurId;
    private Double temperature;
    private Double humidite;

    public Long getCapteurId() {
        return capteurId;
    }

    public void setCapteurId(Long capteurId) {
        this.capteurId = capteurId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidite() {
        return humidite;
    }

    public void setHumidite(Double humidite) {
        this.humidite = humidite;
    }
}
