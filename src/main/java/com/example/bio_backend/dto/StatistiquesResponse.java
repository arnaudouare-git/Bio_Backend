package com.example.bio_backend.dto;

import java.math.BigDecimal;

/** Statistiques globales de la plateforme, reservees a l'Administrateur (module Tier 2, 2026-09-24). */
public class StatistiquesResponse {

    private long nombreProducteurs;
    private long nombreProducteursVerifies;
    private long nombreClients;
    private long nombreCommandes;
    private long nombreCommandesLivrees;
    private BigDecimal chiffreAffairesTotal;
    /** Somme des commissions plateforme sur les paiements REUSSI (ajoute le 2026-09-24, retour client). */
    private BigDecimal commissionTotale;
    private long nombreProduitsPublies;
    private long nombreFormations;
    private long nombreParticipantsFormes;
    private long nombreAvis;
    private Double noteMoyenne;
    private long nombreLitigesOuverts;

    public StatistiquesResponse(long nombreProducteurs, long nombreProducteursVerifies, long nombreClients,
                                 long nombreCommandes, long nombreCommandesLivrees, BigDecimal chiffreAffairesTotal,
                                 BigDecimal commissionTotale, long nombreProduitsPublies, long nombreFormations,
                                 long nombreParticipantsFormes, long nombreAvis, Double noteMoyenne,
                                 long nombreLitigesOuverts) {
        this.nombreProducteurs = nombreProducteurs;
        this.nombreProducteursVerifies = nombreProducteursVerifies;
        this.nombreClients = nombreClients;
        this.nombreCommandes = nombreCommandes;
        this.nombreCommandesLivrees = nombreCommandesLivrees;
        this.chiffreAffairesTotal = chiffreAffairesTotal;
        this.commissionTotale = commissionTotale;
        this.nombreProduitsPublies = nombreProduitsPublies;
        this.nombreFormations = nombreFormations;
        this.nombreParticipantsFormes = nombreParticipantsFormes;
        this.nombreAvis = nombreAvis;
        this.noteMoyenne = noteMoyenne;
        this.nombreLitigesOuverts = nombreLitigesOuverts;
    }

    public long getNombreProducteurs() {
        return nombreProducteurs;
    }

    public long getNombreProducteursVerifies() {
        return nombreProducteursVerifies;
    }

    public long getNombreClients() {
        return nombreClients;
    }

    public long getNombreCommandes() {
        return nombreCommandes;
    }

    public long getNombreCommandesLivrees() {
        return nombreCommandesLivrees;
    }

    public BigDecimal getChiffreAffairesTotal() {
        return chiffreAffairesTotal;
    }

    public BigDecimal getCommissionTotale() {
        return commissionTotale;
    }

    public long getNombreProduitsPublies() {
        return nombreProduitsPublies;
    }

    public long getNombreFormations() {
        return nombreFormations;
    }

    public long getNombreParticipantsFormes() {
        return nombreParticipantsFormes;
    }

    public long getNombreAvis() {
        return nombreAvis;
    }

    public Double getNoteMoyenne() {
        return noteMoyenne;
    }

    public long getNombreLitigesOuverts() {
        return nombreLitigesOuverts;
    }
}
