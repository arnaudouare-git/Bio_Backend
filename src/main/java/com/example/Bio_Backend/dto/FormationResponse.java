package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/**
 * Reponse renvoyee pour une session de formation.
 *
 * nombreParticipants EST le chiffre de traçabilite demande : combien de
 * producteurs ont suivi cette session (voir Formation.participants).
 */
public class FormationResponse {

    private Long id;
    private String titre;
    private LocalDateTime dateSession;
    private String lieu;
    private String formateur;
    private int nombreParticipants;

    public FormationResponse(Long id, String titre, LocalDateTime dateSession, String lieu,
                              String formateur, int nombreParticipants) {
        this.id = id;
        this.titre = titre;
        this.dateSession = dateSession;
        this.lieu = lieu;
        this.formateur = formateur;
        this.nombreParticipants = nombreParticipants;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public LocalDateTime getDateSession() {
        return dateSession;
    }

    public String getLieu() {
        return lieu;
    }

    public String getFormateur() {
        return formateur;
    }

    public int getNombreParticipants() {
        return nombreParticipants;
    }
}
