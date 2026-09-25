package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Litige;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LitigeRepository extends JpaRepository<Litige, Long> {
    List<Litige> findByAuteurId(Long auteurId);
    List<Litige> findByCommandeId(Long commandeId);
    boolean existsByCommandeIdAndStatut(Long commandeId, String statut);
}
