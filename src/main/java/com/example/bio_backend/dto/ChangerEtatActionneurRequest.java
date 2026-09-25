package com.example.bio_backend.dto;

/** Corps de PUT /api/actionneurs/{id}/etat (pilotage manuel). */
public class ChangerEtatActionneurRequest {

    private String etat;

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
}
