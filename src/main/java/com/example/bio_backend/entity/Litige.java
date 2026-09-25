package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Litige ouvert par un acheteur sur une commande payee mais pas encore
 * livree (module ajoute le 2026-09-24, priorite Tier 2) -- permet a
 * l'administrateur d'arbitrer ce cas plutot que de laisser l'acheteur sans
 * recours.
 *
 * statut possibles : OUVERT (en attente d'arbitrage), RESOLU (tranche en
 * faveur de l'acheteur), REJETE (litige juge non fonde par l'admin).
 * Volontairement pas de cascade automatique sur la Commande a la
 * resolution : l'admin garde la main pour ajuster son statut separement via
 * PUT /api/commandes/{id}/statut si besoin (rembourser = annuler,
 * confirmer la livraison = LIVREE...) -- le Litige ne fait que tracer la
 * reclamation et la decision prise.
 */
@Entity
@Table(name = "litiges")
public class Litige {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000, nullable = false)
    private String motif;

    @Column(length = 30, nullable = false)
    private String statut;

    @Column(name = "decision_admin", length = 1000)
    private String decisionAdmin;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = "OUVERT";
        }
    }

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDecisionAdmin() {
        return decisionAdmin;
    }

    public void setDecisionAdmin(String decisionAdmin) {
        this.decisionAdmin = decisionAdmin;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }

    public void setAuteur(Utilisateur auteur) {
        this.auteur = auteur;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateResolution() {
        return dateResolution;
    }

    public void setDateResolution(LocalDateTime dateResolution) {
        this.dateResolution = dateResolution;
    }
}
