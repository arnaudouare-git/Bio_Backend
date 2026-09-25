package com.example.bio_backend.dto;

/**
 * Corps de la requete POST /api/capteurs (installer un capteur dans une serre).
 * type attendu, par exemple : "TEMPERATURE_HUMIDITE".
 */
public class CreerCapteurRequest {

    private Long serreId;
    private String type;

    public Long getSerreId() {
        return serreId;
    }

    public void setSerreId(Long serreId) {
        this.serreId = serreId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
