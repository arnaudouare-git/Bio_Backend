package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/** Corps de la requete POST /api/admin/formations (creer une session de formation). */
public class CreerFormationRequest {

    private String titre;
    private LocalDateTime dateSession;
    private String lieu;
    private String formateur;

    public CreerFormationRequest() {
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public LocalDateTime getDateSession() {
        return dateSession;
    }

    public void setDateSession(LocalDateTime dateSession) {
        this.dateSession = dateSession;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getFormateur() {
        return formateur;
    }

    public void setFormateur(String formateur) {
        this.formateur = formateur;
    }
}
