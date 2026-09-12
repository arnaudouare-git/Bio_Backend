package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Serre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SerreRepository extends JpaRepository<Serre, Long> {
    List<Serre> findByProducteurId(Long producteurId);
}
