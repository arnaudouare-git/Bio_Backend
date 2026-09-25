package com.example.bio_backend.dto;

public class ResoudreLitigeRequest {

    private String statut;
    private String decisionAdmin;

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDecisionAdmin() {
        return decisionAdmin;
    }

    public void setDecisionAdmin(String decisionAdmin) {
        this.decisionAdmin = decisionAdmin;
    }
}
