package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Producteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProducteurRepository extends JpaRepository<Producteur, Long> {
    Optional<Producteur> findByEmail(String email);

    // Utile pour le module Marketplace : recherche par proximite.
    // (une vraie recherche geo se fera plus tard avec une formule de distance
    // ou une extension PostgreSQL comme PostGIS -- ceci est un point de depart)
    List<Producteur> findAll();
}
