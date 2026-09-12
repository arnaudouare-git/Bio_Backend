package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dispositif IoT physique installe dans une Serre
 * (mesure temperature/humidite).
 */
@Entity
@Table(name = "capteurs")
public class Capteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    @Column(name = "date_installation")
    private LocalDateTime dateInstallation;

    @ManyToOne
    @JoinColumn(name = "serre_id", nullable = false)
    private Serre serre;

    @OneToMany(mappedBy = "capteur", cascade = CascadeType.ALL)
    private List<Mesure> mesures = new ArrayList<>();

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDateInstallation() {
        return dateInstallation;
    }

    public void setDateInstallation(LocalDateTime dateInstallation) {
        this.dateInstallation = dateInstallation;
    }

    public Serre getSerre() {
        return serre;
    }

    public void setSerre(Serre serre) {
        this.serre = serre;
    }

    public List<Mesure> getMesures() {
        return mesures;
    }

    public void setMesures(List<Mesure> mesures) {
        this.mesures = mesures;
    }
}
