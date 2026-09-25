package com.example.bio_backend.dto;

import java.math.BigDecimal;

/**
 * Reponse renvoyee pour un produit du catalogue.
 *
 * "distanceKm" (ajoute le 2026-09-24, module Recherche geolocalisee) reste
 * null pour le catalogue classique (GET /api/produits) -- il n'est rempli
 * que par ProduitService.rechercherProduitsProximite(), via setDistanceKm(),
 * une fois la distance calculee par rapport au point de recherche.
 */
public class ProduitResponse {

    private Long id;
    private String nom;
    private String etat;
    private BigDecimal prixUnitaire;
    private Integer stock;
    private Long producteurId;
    private String producteurNom;
    private Double distanceKm;

    public ProduitResponse(Long id, String nom, String etat, BigDecimal prixUnitaire, Integer stock,
                            Long producteurId, String producteurNom) {
        this.id = id;
        this.nom = nom;
        this.etat = etat;
        this.prixUnitaire = prixUnitaire;
        this.stock = stock;
        this.producteurId = producteurId;
        this.producteurNom = producteurNom;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getEtat() {
        return etat;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public Integer getStock() {
        return stock;
    }

    public Long getProducteurId() {
        return producteurId;
    }

    public String getProducteurNom() {
        return producteurNom;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
