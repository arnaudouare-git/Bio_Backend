package com.example.bio_backend.dto;

import java.math.BigDecimal;

/**
 * Une ligne dans la reponse d'une commande (produit + quantite + sous-total).
 *
 * "ligneId" et "statut" (ajoutes le 2026-09-23) : ligneId sert a cibler
 * PUT /api/commandes/{commandeId}/lignes/{ligneId}/statut, et statut suit
 * desormais la progression PAR PRODUCTEUR (chacun avance ses propres
 * lignes independamment des autres).
 */
public class LigneCommandeResponse {

    private Long ligneId;
    private Long produitId;
    private String produitNom;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;
    private String statut;
    private String motifRefus;

    public LigneCommandeResponse(Long ligneId, Long produitId, String produitNom, Integer quantite,
                                  BigDecimal prixUnitaire, BigDecimal sousTotal, String statut,
                                  String motifRefus) {
        this.ligneId = ligneId;
        this.produitId = produitId;
        this.produitNom = produitNom;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.sousTotal = sousTotal;
        this.statut = statut;
        this.motifRefus = motifRefus;
    }

    public Long getLigneId() {
        return ligneId;
    }

    public Long getProduitId() {
        return produitId;
    }

    public String getProduitNom() {
        return produitNom;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public BigDecimal getSousTotal() {
        return sousTotal;
    }

    public String getStatut() {
        return statut;
    }

    public String getMotifRefus() {
        return motifRefus;
    }
}
