package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Utile pour le login : cherche par email peu importe le type
// (Producteur ou Administrateur) grace au JOINED inheritance.
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
}
