package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByAcheteurId(Long acheteurId);
    List<Commande> findByStatut(String statut);
}
