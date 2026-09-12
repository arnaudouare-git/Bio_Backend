package com.example.bio_backend.dto;

/**
 * Reponse renvoyee apres inscription ou connexion reussie.
 * Pas de token JWT pour l'instant : la securite complete (Spring Security,
 * roles, tokens) est prevue comme etape "Plus tard" dans le recap projet.
 */
public class AuthResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String statutVerificationCnib;

    public AuthResponse(Long id, String nom, String prenom, String email, String role, String statutVerificationCnib) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.statutVerificationCnib = statutVerificationCnib;
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
}
