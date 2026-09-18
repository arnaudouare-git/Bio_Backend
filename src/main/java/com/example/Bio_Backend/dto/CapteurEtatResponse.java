package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/**
 * Etat "temps reel" d'un capteur, utilise dans le dashboard d'une serre
 * (GET /api/serres/{id}/dashboard). "silencieux" est calcule a la demande
 * (pas de tache planifiee @Scheduled pour l'instant) : vrai si plus de
 * 5 minutes se sont ecoulees depuis la derniere mesure recue -- ou s'il
 * n'y a encore jamais eu de mesure du tout.
 */
public class CapteurEtatResponse {

    private Long capteurId;
    private String type;
    private Double derniereTemperature;
    private Double derniereHumidite;
    private LocalDateTime dateDerniereMesure;
    private boolean silencieux;

    public CapteurEtatResponse(Long capteurId, String type, Double derniereTemperature,
                                Double derniereHumidite, LocalDateTime dateDerniereMesure,
                                boolean silencieux) {
        this.capteurId = capteurId;
        this.type = type;
        this.derniereTemperature = derniereTemperature;
        this.derniereHumidite = derniereHumidite;
        this.dateDerniereMesure = dateDerniereMesure;
        this.silencieux = silencieux;
    }

    public Long getCapteurId() {
        return capteurId;
    }

    public String getType() {
        return type;
    }

    public Double getDerniereTemperature() {
        return derniereTemperature;
    }

    public Double getDerniereHumidite() {
        return derniereHumidite;
    }

    public LocalDateTime getDateDerniereMesure() {
        return dateDerniereMesure;
    }

    public boolean isSilencieux() {
        return silencieux;
    }
}
