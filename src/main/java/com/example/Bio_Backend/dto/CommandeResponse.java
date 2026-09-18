package com.example.bio_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Reponse renvoyee pour une commande. */
public class CommandeResponse {

    private Long id;
    private LocalDateTime dateCommande;
    private String statut;
    private BigDecimal montantTotal;
    private String adresseLivraison;
    private Long acheteurId;
    private String acheteurNom;
    private List<LigneCommandeResponse> lignes;

    public CommandeResponse(Long id, LocalDateTime dateCommande, String statut, BigDecimal montantTotal,
                             String adresseLivraison, Long acheteurId, String acheteurNom,
                             List<LigneCommandeResponse> lignes) {
        this.id = id;
        this.dateCommande = dateCommande;
        this.statut = statut;
        this.montantTotal = montantTotal;
        this.adresseLivraison = adresseLivraison;
        this.acheteurId = acheteurId;
        this.acheteurNom = acheteurNom;
        this.lignes = lignes;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public String getStatut() {
        return statut;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public Long getAcheteurId() {
        return acheteurId;
    }

    public String getAcheteurNom() {
        return acheteurNom;
    }

    public List<LigneCommandeResponse> getLignes() {
        return lignes;
    }
}
