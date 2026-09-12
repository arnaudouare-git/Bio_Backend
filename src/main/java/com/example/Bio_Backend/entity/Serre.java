package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Unite de production physique appartenant a un Producteur
 * (module IoT - Module 1 du cahier des charges).
 */
@Entity
@Table(name = "serres")
public class Serre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    private String localisation;

    @Column(name = "capacite_max")
    private Integer capaciteMax;

    @ManyToOne
    @JoinColumn(name = "producteur_id", nullable = false)
    private Producteur producteur;

    @OneToMany(mappedBy = "serre", cascade = CascadeType.ALL)
    private List<Capteur> capteurs = new ArrayList<>();

    @OneToMany(mappedBy = "serre", cascade = CascadeType.ALL)
    private List<Alerte> alertes = new ArrayList<>();

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Producteur getProducteur() {
        return producteur;
    }

    public void setProducteur(Producteur producteur) {
        this.producteur = producteur;
    }

    public List<Capteur> getCapteurs() {
        return capteurs;
    }

    public void setCapteurs(List<Capteur> capteurs) {
        this.capteurs = capteurs;
    }

    public List<Alerte> getAlertes() {
        return alertes;
    }

    public void setAlertes(List<Alerte> alertes) {
        this.alertes = alertes;
    }
}
