package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Creee automatiquement quand une Mesure depasse un seuil
 * (ex: temperature > 39C, capteur silencieux > 5 min).
 * statut possibles : NON_TRAITEE, TRAITEE
 */
@Entity
@Table(name = "alertes")
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private Double seuil;

    @Column(name = "date_alerte")
    private LocalDateTime dateAlerte;

    @Column(length = 30)
    private String statut;

    @ManyToOne
    @JoinColumn(name = "serre_id", nullable = false)
    private Serre serre;

    @PrePersist
    protected void onCreate() {
        this.dateAlerte = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = "NON_TRAITEE";
        }
    }

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

    public Double getSeuil() {
        return seuil;
    }

    public void setSeuil(Double seuil) {
        this.seuil = seuil;
    }

    public LocalDateTime getDateAlerte() {
        return dateAlerte;
    }

    public void setDateAlerte(LocalDateTime dateAlerte) {
        this.dateAlerte = dateAlerte;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Serre getSerre() {
        return serre;
    }

    public void setSerre(Serre serre) {
        this.serre = serre;
    }
}
