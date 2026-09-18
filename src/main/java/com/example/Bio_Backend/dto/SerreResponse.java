package com.example.bio_backend.dto;

/** Reponse renvoyee pour une serre. */
public class SerreResponse {

    private Long id;
    private String nom;
    private String localisation;
    private Integer capaciteMax;
    private Long producteurId;
    private String producteurNom;
    private int nombreCapteurs;

    public SerreResponse(Long id, String nom, String localisation, Integer capaciteMax,
                          Long producteurId, String producteurNom, int nombreCapteurs) {
        this.id = id;
        this.nom = nom;
        this.localisation = localisation;
        this.capaciteMax = capaciteMax;
        this.producteurId = producteurId;
        this.producteurNom = producteurNom;
        this.nombreCapteurs = nombreCapteurs;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getLocalisation() {
        return localisation;
    }

    public Integer getCapaciteMax() {
        return capaciteMax;
    }

    public Long getProducteurId() {
        return producteurId;
    }

    public String getProducteurNom() {
        return producteurNom;
    }

    public int getNombreCapteurs() {
        return nombreCapteurs;
    }
}
