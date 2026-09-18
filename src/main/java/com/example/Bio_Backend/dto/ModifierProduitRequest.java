package com.example.bio_backend.dto;

import java.math.BigDecimal;

/**
 * Corps de la requete PUT /api/produits/{id} (modifier un produit existant :
 * prix, stock, etat...). Pas de producteurId ici -- on ne change jamais le
 * proprietaire d'un produit apres coup, seulement son contenu.
 */
public class ModifierProduitRequest {

    private String nom;
    private String etat;
    private BigDecimal prixUnitaire;
    private Integer stock;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
