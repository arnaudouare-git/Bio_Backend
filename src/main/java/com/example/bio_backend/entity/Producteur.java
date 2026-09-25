package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represente un Producteur : produit, vend ET achete des larves.
 *
 * v3 du modele (2026-09-11) : le Producteur n'est plus le seul role
 * acheteur -- un second role, Client (achete seulement, voir Client.java),
 * existe desormais. Le Producteur reste toutefois le SEUL a pouvoir
 * produire et vendre, et reste seul soumis a la formation obligatoire +
 * verification CNIB avant activation du compte (voir Formation,
 * AdminService.activerCompte) -- y compris pour acheter.
 *
 * Herite des champs communs de Utilisateur + latitude/longitude pour le
 * module Marketplace geolocalise.
 */
@Entity
@Table(name = "producteurs")
public class Producteur extends Utilisateur {

    private Double latitude;
    private Double longitude;

    @OneToMany(mappedBy = "producteur", cascade = CascadeType.ALL)
    private List<Produit> produits = new ArrayList<>();

    @OneToMany(mappedBy = "producteur", cascade = CascadeType.ALL)
    private List<Serre> serres = new ArrayList<>();

    // NB : il n'y a plus de collection bidirectionnelle "commandesPassees"
    // ici -- Commande.acheteur est desormais type Utilisateur (Producteur OU
    // Client), ce qui rend impossible un @OneToMany(mappedBy="acheteur")
    // type Producteur (mappedBy exige que le champ cible ait le meme type
    // que la classe proprietaire de la collection). Pour recuperer les
    // commandes passees par CE producteur en tant qu'acheteur, utiliser
    // CommandeRepository.findByAcheteurId(this.getId()).

    // La session de formation (hors plateforme) suivie par ce producteur.
    // Reste null tant qu'il n'a pas ete enregistre comme participant par un
    // Administrateur (voir AdminService.enregistrerParticipant) -- et c'est
    // une condition prealable a l'activation du compte
    // (voir AdminService.activerCompte).
    @ManyToOne
    @JoinColumn(name = "formation_id")
    private Formation formation;

    // ---------- getters / setters ----------

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    }

    public List<Serre> getSerres() {
        return serres;
    }

    public void setSerres(List<Serre> serres) {
        this.serres = serres;
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }
}
