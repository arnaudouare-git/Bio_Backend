package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findByProducteurId(Long producteurId);
    List<Produit> findByStockGreaterThan(Integer stock);
}
