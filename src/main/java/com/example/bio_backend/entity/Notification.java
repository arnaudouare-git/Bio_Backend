package com.example.bio_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Garde une trace des SMS/emails envoyes (confirmation de commande,
 * activation de compte, alerte, etc.) a un Utilisateur (Producteur ou
 * Administrateur). Depuis le 2026-09-24, sert aussi de notification
 * interne a l'application (canal "APP", voir NotificationService) --
 * pas d'envoi reel, juste un enregistrement consultable par son
 * destinataire.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destinataire;

    // SMS, EMAIL, APP (notification interne, sans envoi reel)
    private String canal;

    @Column(length = 1000)
    private String message;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    // EN_ATTENTE, ENVOYE, ECHOUE
    @Column(name = "statut_envoi")
    private String statutEnvoi;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @PrePersist
    protected void onCreate() {
        this.dateEnvoi = LocalDateTime.now();
        if (this.statutEnvoi == null) {
            this.statutEnvoi = "EN_ATTENTE";
        }
    }

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(String destinataire) {
        this.destinataire = destinataire;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public String getStatutEnvoi() {
        return statutEnvoi;
    }

    public void setStatutEnvoi(String statutEnvoi) {
        this.statutEnvoi = statutEnvoi;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}
