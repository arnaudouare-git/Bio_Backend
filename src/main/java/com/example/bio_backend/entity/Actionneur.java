package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Dispositif de pilotage automatique installe dans une Serre (ajoute le
 * 2026-09-24, retour client en entretien : "pilotage automatique pour
 * arroser la serre"). Simule un actionneur physique -- pas de vrai
 * materiel pilote -- dont l'etat est change automatiquement par
 * MesureService en fonction des mesures d'humidite (meme mecanisme que les
 * Alertes, voir MesureService.enregistrerMesure()), et consultable/
 * pilotable manuellement via l'API.
 *
 * type possibles : IRRIGATION (extensible plus tard : VENTILATION...)
 * etat possibles : ACTIF, INACTIF
 */
@Entity
@Table(name = "actionneurs")
public class Actionneur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String type;

    @Column(length = 20)
    private String etat;

    @Column(name = "date_dernier_changement")
    private LocalDateTime dateDernierChangement;

    @ManyToOne
    @JoinColumn(name = "serre_id", nullable = false)
    private Serre serre;

    @PrePersist
    protected void onCreate() {
        if (this.etat == null) {
            this.etat = "INACTIF";
        }
        if (this.dateDernierChangement == null) {
            this.dateDernierChangement = LocalDateTime.now();
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

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public LocalDateTime getDateDernierChangement() {
        return dateDernierChangement;
    }

    public void setDateDernierChangement(LocalDateTime dateDernierChangement) {
        this.dateDernierChangement = dateDernierChangement;
    }

    public Serre getSerre() {
        return serre;
    }

    public void setSerre(Serre serre) {
        this.serre = serre;
    }
}
