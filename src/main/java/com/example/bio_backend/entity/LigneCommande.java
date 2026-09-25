package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Detail d'une commande : un produit + une quantite.
 * Le prix_unitaire est duplique ici (au moment de l'achat)
 * pour rester correct meme si le prix du Produit change plus tard.
 *
 * "statut" (ajoute le 2026-09-23) : chaque ligne suit desormais son propre
 * statut (EN_ATTENTE, CONFIRMEE, EN_PREPARATION, EXPEDIEE, LIVREE, ANNULEE),
 * pour que le Producteur qui possede ce produit puisse faire avancer SA
 * ligne sans toucher aux lignes des autres Producteurs dans la meme
 * commande. Voir CommandeService.changerStatutLigne().
 */
@Entity
@Table(name = "lignes_commande")
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire", nullable = false)
    private BigDecimal prixUnitaire;

    @Column(length = 30)
    private String statut;

    // Rempli uniquement quand statut == "REFUSEE" (module Refus de commande,
    // 2026-09-24) : raison donnee par le Producteur (ou l'Admin) pour ne pas
    // honorer cette ligne. Voir CommandeService.changerStatutLigne().
    @Column(name = "motif_refus", length = 500)
    private String motifRefus;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @PrePersist
    protected void onCreate() {
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

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }
}
