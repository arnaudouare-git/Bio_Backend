package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/** Reponse renvoyee pour un litige. */
public class LitigeResponse {

    private Long id;
    private Long commandeId;
    private Long auteurId;
    private String auteurNom;
    private String motif;
    private String statut;
    private String decisionAdmin;
    private LocalDateTime dateCreation;
    private LocalDateTime dateResolution;

    public LitigeResponse(Long id, Long commandeId, Long auteurId, String auteurNom, String motif,
                           String statut, String decisionAdmin, LocalDateTime dateCreation,
                           LocalDateTime dateResolution) {
        this.id = id;
        this.commandeId = commandeId;
        this.auteurId = auteurId;
        this.auteurNom = auteurNom;
        this.motif = motif;
        this.statut = statut;
        this.decisionAdmin = decisionAdmin;
        this.dateCreation = dateCreation;
        this.dateResolution = dateResolution;
    }

    public Long getId() {
        return id;
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public Long getAuteurId() {
        return auteurId;
    }

    public String getAuteurNom() {
        return auteurNom;
    }

    public String getMotif() {
        return motif;
    }

    public String getStatut() {
        return statut;
    }

    public String getDecisionAdmin() {
        return decisionAdmin;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public LocalDateTime getDateResolution() {
        return dateResolution;
    }
}
