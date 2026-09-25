package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Une tentative de paiement pour une Commande.
 * Une commande peut avoir PLUSIEURS paiements (tentatives echouees + reussie).
 * statut possibles : EN_ATTENTE, REUSSI, ECHOUE
 */
@Entity
@Table(name = "paiements")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_transaction")
    private String referenceTransaction;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(length = 30)
    private String statut;

    @Column(nullable = false)
    private BigDecimal montant;

    /** Moyen de paiement : "ORANGE_MONEY" ou "CASH" (especes a la livraison). */
    @Column(length = 30, nullable = false)
    private String methode;

    /**
     * Commission plateforme (ajoutee le 2026-09-24, retour client), calculee
     * une seule fois au moment ou le paiement passe REUSSI (voir
     * PaiementService.confirmerPaiement()) -- reste null tant que ce n'est
     * pas le cas. Pas de vrai prelevement : juste trace pour les stats admin.
     */
    @Column(name = "montant_commission")
    private BigDecimal montantCommission;

    /** montant - montantCommission -- ce qui revient au Producteur. */
    @Column(name = "montant_net_producteur")
    private BigDecimal montantNetProducteur;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @PrePersist
    protected void onCreate() {
        this.datePaiement = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = "EN_ATTENTE";
        }
    }

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceTransaction() {
        return referenceTransaction;
    }

    public void setReferenceTransaction(String referenceTransaction) {
        this.referenceTransaction = referenceTransaction;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getMethode() {
        return methode;
    }

    public void setMethode(String methode) {
        this.methode = methode;
    }

    public BigDecimal getMontantCommission() {
        return montantCommission;
    }

    public void setMontantCommission(BigDecimal montantCommission) {
        this.montantCommission = montantCommission;
    }

    public BigDecimal getMontantNetProducteur() {
        return montantNetProducteur;
    }

    public void setMontantNetProducteur(BigDecimal montantNetProducteur) {
        this.montantNetProducteur = montantNetProducteur;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }
}
