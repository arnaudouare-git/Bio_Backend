package com.example.bio_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Reponse renvoyee pour un paiement. */
public class PaiementResponse {

    private Long id;
    private Long commandeId;
    private BigDecimal montant;
    private String methode;
    private String statut;
    private String referenceTransaction;
    private LocalDateTime datePaiement;

    public PaiementResponse(Long id, Long commandeId, BigDecimal montant, String methode,
                             String statut, String referenceTransaction, LocalDateTime datePaiement) {
        this.id = id;
        this.commandeId = commandeId;
        this.montant = montant;
        this.methode = methode;
        this.statut = statut;
        this.referenceTransaction = referenceTransaction;
        this.datePaiement = datePaiement;
    }

    public Long getId() {
        return id;
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public String getMethode() {
        return methode;
    }

    public String getStatut() {
        return statut;
    }

    public String getReferenceTransaction() {
        return referenceTransaction;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }
}
