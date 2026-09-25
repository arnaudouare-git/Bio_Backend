package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {
    List<Avis> findByProducteurId(Long producteurId);

    boolean existsByAuteurIdAndCommandeIdAndProducteurId(Long auteurId, Long commandeId, Long producteurId);
}
