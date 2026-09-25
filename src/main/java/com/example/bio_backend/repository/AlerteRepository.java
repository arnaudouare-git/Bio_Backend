package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    List<Alerte> findBySerreIdAndStatut(Long serreId, String statut);
}
