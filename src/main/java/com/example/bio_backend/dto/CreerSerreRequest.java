package com.example.bio_backend.dto;

/** Corps de la requete POST /api/serres (creer une serre pour un Producteur VERIFIE). */
public class CreerSerreRequest {

    private Long producteurId;
    private String nom;
    private String localisation;
    private Integer capaciteMax;

    public Long getProducteurId() {
        return producteurId;
    }

    public void setProducteurId(Long producteurId) {
        this.producteurId = producteurId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public Integer getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(Integer capaciteMax) {
        this.capaciteMax = capaciteMax;
    }
}
