package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Chaque releve envoye par un Capteur (une ligne = une lecture
 * a un instant donne). C'est ici que vos capteurs IoT ecriront
 * leurs donnees en continu.
 */
@Entity
@Table(name = "mesures")
public class Mesure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double temperature;

    private Double humidite;

    @Column(name = "date_mesure")
    private LocalDateTime dateMesure;

    @ManyToOne
    @JoinColumn(name = "capteur_id", nullable = false)
    private Capteur capteur;

    @PrePersist
    protected void onCreate() {
        if (this.dateMesure == null) {
            this.dateMesure = LocalDateTime.now();
        }
    }

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidite() {
        return humidite;
    }

    public void setHumidite(Double humidite) {
        this.humidite = humidite;
    }

    public LocalDateTime getDateMesure() {
        return dateMesure;
    }

    public void setDateMesure(LocalDateTime dateMesure) {
        this.dateMesure = dateMesure;
    }

    public Capteur getCapteur() {
        return capteur;
    }

    public void setCapteur(Capteur capteur) {
        this.capteur = capteur;
    }
}
