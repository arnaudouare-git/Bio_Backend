package com.example.bio_backend.config;

import com.example.bio_backend.entity.Client;
import com.example.bio_backend.entity.Producteur;
import com.example.bio_backend.entity.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapte notre entite Utilisateur (Producteur/Client/Administrateur) au
 * format que Spring Security comprend (interface UserDetails).
 *
 * Le "role" Spring Security (ROLE_...) est deduit du type REEL de l'objet
 * (grace a l'heritage JOINED) -- exactement la meme logique que
 * AuthService.versReponse() utilise deja pour remplir le champ "role" de
 * la reponse JSON, juste appliquee ici pour la securite plutot que pour
 * l'affichage.
 */
public class UtilisateurDetailsImpl implements UserDetails {

    private final Utilisateur utilisateur;

    public UtilisateurDetailsImpl(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role;
        if (utilisateur instanceof Producteur) {
            role = "ROLE_PRODUCTEUR";
        } else if (utilisateur instanceof Client) {
            role = "ROLE_CLIENT";
        } else {
            role = "ROLE_ADMINISTRATEUR";
        }
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return utilisateur.getMotDePasse();
    }

    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
