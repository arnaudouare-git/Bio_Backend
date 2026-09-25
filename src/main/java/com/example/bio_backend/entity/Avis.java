package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Avis (note de 1 a 5 + commentaire optionnel) laisse par un acheteur sur un
 * Producteur, APRES avoir reellement recu une commande livree aupres de lui.
 *
 * Module ajoute le 2026-09-24 (priorite Tier 1). Toujours rattache a la
 * Commande qui justifie l'avis (voir AvisService.laisserAvis) : impossible
 * de noter un Producteur sans avoir reellement achete et recu ses produits,
 * et impossible de laisser deux avis pour le meme (auteur, commande,
 * producteur).
 */
@Entity
@Table(name = "avis")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer note;

    @Column(length = 1000)
    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "producteur_id", nullable = false)
    private Producteur producteur;

    @ManyToOne
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Producteur getProducteur() {
        return producteur;
    }

    public void setProducteur(Producteur producteur) {
        this.producteur = producteur;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }

    public void setAuteur(Utilisateur auteur) {
        this.auteur = auteur;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
