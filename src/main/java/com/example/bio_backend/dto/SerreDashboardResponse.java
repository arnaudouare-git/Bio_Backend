package com.example.bio_backend.dto;

import java.util.List;

/**
 * Vue d'ensemble "temps reel" d'une serre : etat de chaque capteur +
 * alertes non traitees. C'est cette reponse qui alimenterait l'ecran
 * "Dashboard IoT Serre" du frontend.
 */
public class SerreDashboardResponse {

    private Long serreId;
    private String nomSerre;
    private List<CapteurEtatResponse> capteurs;
    private List<AlerteResponse> alertesNonTraitees;

    public SerreDashboardResponse(Long serreId, String nomSerre, List<CapteurEtatResponse> capteurs,
                                   List<AlerteResponse> alertesNonTraitees) {
        this.serreId = serreId;
        this.nomSerre = nomSerre;
        this.capteurs = capteurs;
        this.alertesNonTraitees = alertesNonTraitees;
    }

    public Long getSerreId() {
        return serreId;
    }

    public String getNomSerre() {
        return nomSerre;
    }

    public List<CapteurEtatResponse> getCapteurs() {
        return capteurs;
    }

    public List<AlerteResponse> getAlertesNonTraitees() {
        return alertesNonTraitees;
    }
}
