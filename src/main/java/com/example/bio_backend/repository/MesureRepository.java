package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Mesure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MesureRepository extends JpaRepository<Mesure, Long> {
    // Utile pour l'historique 7 jours demande dans le CDC
    List<Mesure> findTop50ByCapteurIdOrderByDateMesureDesc(Long capteurId);
}
