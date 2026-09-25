package com.example.bio_backend.repository;

import com.example.bio_backend.entity.Actionneur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActionneurRepository extends JpaRepository<Actionneur, Long> {
    List<Actionneur> findBySerreId(Long serreId);

    Optional<Actionneur> findBySerreIdAndType(Long serreId, String type);
}
