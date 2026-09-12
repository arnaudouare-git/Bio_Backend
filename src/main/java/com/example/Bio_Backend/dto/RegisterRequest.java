package com.example.bio_backend.dto;

/**
 * Corps de la requete POST /api/auth/inscription.
 *
 * v3 du modele (2026-09-11) : le champ "role" est de retour, avec DEUX
 * valeurs possibles (voir RoleUtilisateur) :
 *   - PRODUCTEUR : documentIdentiteRef (piece d'identite CNIB) est
 *     obligatoire -- c'est ce qu'un Administrateur verifiera manuellement
 *     avant d'activer le compte (voir AdminService.activerCompte). Le
 *     compte reste au statut EN_ATTENTE tant que ce n'est pas fait -- et
 *     tant que la formation obligatoire (hors plateforme) n'a pas ete
 *     enregistree par un admin.
 *   - CLIENT : documentIdentiteRef n'est PAS requis. Le compte est actif
 *     immediatement (voir AuthService.inscrireClient).
 */
public class RegisterRequest {

    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private RoleUtilisateur role;
    private String documentIdentiteRef;

    private Double latitude;
    private Double longitude;

    public RegisterRequest() {
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public RoleUtilisateur getRole() {
        return role;
    }

    public void setRole(RoleUtilisateur role) {
        this.role = role;
    }

    public String getDocumentIdentiteRef() {
        return documentIdentiteRef;
    }

    public void setDocumentIdentiteRef(String documentIdentiteRef) {
        this.documentIdentiteRef = documentIdentiteRef;
    }

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
}
