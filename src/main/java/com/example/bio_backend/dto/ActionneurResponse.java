package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/** Reponse renvoyee pour un actionneur (module ajoute le 2026-09-24, retour client). */
public class ActionneurResponse {

    private Long id;
    private String type;
    private String etat;
    private LocalDateTime dateDernierChangement;
    private Long serreId;

    public ActionneurResponse(Long id, String type, String etat, LocalDateTime dateDernierChangement, Long serreId) {
        this.id = id;
        this.type = type;
        this.etat = etat;
        this.dateDernierChangement = dateDernierChangement;
        this.serreId = serreId;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getEtat() {
        return etat;
    }

    public LocalDateTime getDateDernierChangement() {
        return dateDernierChangement;
    }

    public Long getSerreId() {
        return serreId;
    }
}
