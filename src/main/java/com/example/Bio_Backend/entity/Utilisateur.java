package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Classe mere abstraite : contient les informations COMMUNES
 * a tous les types d'utilisateurs (Producteur, Client, Administrateur).
 * On ne cree jamais un objet Utilisateur directement.
 */
@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 20)
    private String telephone;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    // EN_ATTENTE, VERIFIE, REJETE -- exigence CNIB du cahier des charges.
    // Un Producteur reste EN_ATTENTE tant qu'un Administrateur n'a pas
    // verifie sa piece d'identite (voir AdminService.activerCompte), ce qui
    // n'est lui-meme possible qu'apres avoir suivi la formation obligatoire
    // (voir Producteur.formation). Un Client, lui, est mis directement a
    // VERIFIE des sa creation (voir AuthService.inscrireClient) : il n'a ni
    // formation ni CNIB a fournir, mais ce meme champ reste utilisable par
    // un futur controle d'acces unique cote achat, quel que soit le role.
    @Column(name = "statut_verification_cnib", length = 30)
    private String statutVerificationCnib;

    @Column(name = "document_identite_ref")
    private String documentIdentiteRef;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        if (this.statutVerificationCnib == null) {
            this.statutVerificationCnib = "EN_ATTENTE";
        }
    }

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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
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

    public String getStatutVerificationCnib() {
        return statutVerificationCnib;
    }

    public void setStatutVerificationCnib(String statutVerificationCnib) {
        this.statutVerificationCnib = statutVerificationCnib;
    }

    public String getDocumentIdentiteRef() {
        return documentIdentiteRef;
    }

    public void setDocumentIdentiteRef(String documentIdentiteRef) {
        this.documentIdentiteRef = documentIdentiteRef;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
