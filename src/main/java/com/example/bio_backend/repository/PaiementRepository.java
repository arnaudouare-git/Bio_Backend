package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByCommandeId(Long commandeId);
}
