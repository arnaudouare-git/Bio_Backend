package com.example.bio_backend.dto;

/**
 * Reponse renvoyee apres inscription ou connexion reussie.
 *
 * Depuis le module Securite (2026-09-18) : contient un "token" JWT. C'est
 * ce token qu'il faut renvoyer dans le header "Authorization: Bearer
 * <token>" de toute requete vers une route protegee (voir SecurityConfig).
 */
public class AuthResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String statutVerificationCnib;
    private String token;

    public AuthResponse(Long id, String nom, String prenom, String email, String role,
                         String statutVerificationCnib, String token) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.statutVerificationCnib = statutVerificationCnib;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getStatutVerificationCnib() {
        return statutVerificationCnib;
    }

    public String getToken() {
        return token;
    }
}
