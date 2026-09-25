package com.example.bio_backend.dto;

/** Corps de la requete POST /api/auth/connexion. */
public class LoginRequest {

    private String email;
    private String motDePasse;

    public LoginRequest() {
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
}
