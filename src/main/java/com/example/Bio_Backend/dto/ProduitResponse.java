package com.example.bio_backend.dto;

import java.math.BigDecimal;

/** Reponse renvoyee pour un produit du catalogue. */
public class ProduitResponse {

    private Long id;
    private String nom;
    private String etat;
    private BigDecimal prixUnitaire;
    private Integer stock;
    private Long producteurId;
    private String producteurNom;

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
}
