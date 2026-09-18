package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/** Reponse renvoyee pour une alerte. */
public class AlerteResponse {

    private Long id;
    private String type;
    private Double seuil;
    private LocalDateTime dateAlerte;
    private String statut;
    private Long serreId;

    public AlerteResponse(Long id, String type, Double seuil, LocalDateTime dateAlerte,
                           String statut, Long serreId) {
        this.id = id;
        this.type = type;
        this.seuil = seuil;
        this.dateAlerte = dateAlerte;
        this.statut = statut;
        this.serreId = serreId;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Double getSeuil() {
        return seuil;
    }

    public LocalDateTime getDateAlerte() {
        return dateAlerte;
    }

    public String getStatut() {
        return statut;
    }

    public Long getSerreId() {
        return serreId;
    }
}
