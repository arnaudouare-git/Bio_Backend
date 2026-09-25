package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Capteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CapteurRepository extends JpaRepository<Capteur, Long> {
    List<Capteur> findBySerreId(Long serreId);
}
