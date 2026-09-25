package com.example.bio_backend.dto;

import java.time.LocalDateTime;

/** Reponse renvoyee pour un avis laisse sur un Producteur. */
public class AvisResponse {

    private Long id;
    private Integer note;
    private String commentaire;
    private Long auteurId;
    private String auteurNom;
    private Long producteurId;
    private LocalDateTime dateCreation;

    public AvisResponse(Long id, Integer note, String commentaire, Long auteurId, String auteurNom,
                         Long producteurId, LocalDateTime dateCreation) {
        this.id = id;
        this.note = note;
        this.commentaire = commentaire;
        this.auteurId = auteurId;
        this.auteurNom = auteurNom;
        this.producteurId = producteurId;
        this.dateCreation = dateCreation;
    }

    public Long getId() {
        return id;
    }

    public Integer getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Long getAuteurId() {
        return auteurId;
    }

    public String getAuteurNom() {
        return auteurNom;
    }

    public Long getProducteurId() {
        return producteurId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
}
