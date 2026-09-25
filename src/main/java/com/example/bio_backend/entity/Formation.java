package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represente une session de formation obligatoire, dispensee HORS de la
 * plateforme (en presentiel), avant qu'un Producteur puisse etre inscrit.
 *
 * La formation elle-meme reste un evenement physique, mais elle DOIT etre
 * tracee ici comme une vraie entite : c'est ce qui permet a l'Administrateur
 * de savoir combien de personnes ont ete formees (traçabilite), et c'est la
 * condition prealable a l'activation du compte d'un Producteur (avec sa
 * piece d'identite CNIB) -- voir AdminService.activerCompte().
 */
@Entity
@Table(name = "formations")
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(name = "date_session")
    private LocalDateTime dateSession;

    private String lieu;

    private String formateur;

    // Tous les Producteurs ayant suivi CETTE session -- sa taille est
    // exactement le chiffre de traçabilite demande ("nombre de personnes
    // qui ont suivi la formation").
    @OneToMany(mappedBy = "formation")
    private List<Producteur> participants = new ArrayList<>();

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<Producteur> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Producteur> participants) {
        this.participants = participants;
    }
}
