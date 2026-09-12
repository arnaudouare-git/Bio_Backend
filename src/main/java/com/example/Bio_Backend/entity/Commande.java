package com.example.bio_backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represente un achat passe par un ACHETEUR aupres d'un Producteur
 * (vendeur, retrouve via LigneCommande -> Produit -> Producteur).
 *
 * v3 du modele (2026-09-11) : l'acheteur peut etre soit un Producteur (qui
 * achete aupres d'un autre producteur), soit un Client (qui achete
 * seulement). D'ou le typage sur la classe abstraite Utilisateur plutot que
 * sur Producteur -- l'heritage JOINED permet a Hibernate de retrouver le
 * type concret reel a la lecture. Pour recuperer les commandes d'un
 * acheteur donne, utiliser CommandeRepository.findByAcheteurId(id) (qui
 * fonctionne pour les deux roles) plutot qu'une collection bidirectionnelle
 * sur Producteur, qui ne serait plus possible avec ce typage polymorphe.
 *
 * statut possibles : EN_ATTENTE, CONFIRMEE, EN_PREPARATION, EXPEDIEE, LIVREE, ANNULEE
 */
@Entity
@Table(name = "commandes")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_commande")
    private LocalDateTime dateCommande;

    @Column(length = 30)
    private String statut;

    @Column(name = "montant_total")
    private BigDecimal montantTotal;

    @Column(name = "adresse_livraison")
    private String adresseLivraison;

    // L'Utilisateur (Producteur OU Client) qui ACHETE cette commande.
    @ManyToOne
    @JoinColumn(name = "acheteur_id", nullable = false)
    private Utilisateur acheteur;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<Paiement> paiements = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCommande = LocalDateTime.now();
        if (this.statut == null) {
            this.statut = "EN_ATTENTE";
        }
    }

    /**
     * Recalcule le montant total a partir des lignes de commande.
     * A appeler dans le service, apres avoir ajoute/modifie les lignes.
     */
    public void calculerMontantTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LigneCommande ligne : lignes) {
            BigDecimal sousTotal = ligne.getPrixUnitaire()
                    .multiply(BigDecimal.valueOf(ligne.getQuantite()));
            total = total.add(sousTotal);
        }
        this.montantTotal = total;
    }

    // ---------- getters / setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public Utilisateur getAcheteur() {
        return acheteur;
    }

    public void setAcheteur(Utilisateur acheteur) {
        this.acheteur = acheteur;
    }

    public List<LigneCommande> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommande> lignes) {
        this.lignes = lignes;
    }

    public List<Paiement> getPaiements() {
        return paiements;
    }

    public void setPaiements(List<Paiement> paiements) {
        this.paiements = paiements;
    }
}
