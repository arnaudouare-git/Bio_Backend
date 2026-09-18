package com.example.bio_backend.service;

import com.example.bio_backend.dto.ChangerStatutCommandeRequest;
import com.example.bio_backend.dto.CommandeResponse;
import com.example.bio_backend.dto.CreerCommandeRequest;
import com.example.bio_backend.dto.LigneCommandeRequest;
import com.example.bio_backend.dto.LigneCommandeResponse;
import com.example.bio_backend.entity.Commande;
import com.example.bio_backend.entity.LigneCommande;
import com.example.bio_backend.entity.Produit;
import com.example.bio_backend.entity.Utilisateur;
import com.example.bio_backend.exception.CompteNonVerifieException;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.exception.StockInsuffisantException;
import com.example.bio_backend.repository.CommandeRepository;
import com.example.bio_backend.repository.ProduitRepository;
import com.example.bio_backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Logique metier du passage de commande.
 *
 * Regle metier centrale de ce module (le controle qu'on reportait depuis le
 * module Authentification, cote ACHAT cette fois -- deja fait cote vente
 * dans ProduitService) : n'importe quel Utilisateur (Producteur OU Client)
 * peut acheter, MAIS seulement si son compte est VERIFIE. Comme l'acheteur
 * est type sur la classe abstraite Utilisateur (heritage JOINED), un seul
 * controle suffit pour les deux roles -- pas de branche par role ici.
 */
@Service
public class CommandeService {

    private static final Set<String> STATUTS_VALIDES = Set.of(
            "EN_ATTENTE", "CONFIRMEE", "EN_PREPARATION", "EXPEDIEE", "LIVREE", "ANNULEE");

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;

    public CommandeService(CommandeRepository commandeRepository,
                            ProduitRepository produitRepository,
                            UtilisateurRepository utilisateurRepository) {
        this.commandeRepository = commandeRepository;
        this.produitRepository = produitRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public CommandeResponse passerCommande(CreerCommandeRequest requete) {
        Utilisateur acheteur = utilisateurRepository.findById(requete.getAcheteurId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Utilisateur introuvable : id=" + requete.getAcheteurId()));

        if (!"VERIFIE".equals(acheteur.getStatutVerificationCnib())) {
            throw new CompteNonVerifieException(
                    "Impossible de passer commande : ce compte n'est pas encore verifie "
                            + "(formation + verification de la piece d'identite requises pour un Producteur ; "
                            + "un Client est normalement verifie des l'inscription).");
        }

        if (requete.getLignes() == null || requete.getLignes().isEmpty()) {
            throw new IllegalArgumentException("Une commande doit contenir au moins une ligne (produit + quantite).");
        }

        Commande commande = new Commande();
        commande.setAcheteur(acheteur);
        commande.setAdresseLivraison(requete.getAdresseLivraison());

        for (LigneCommandeRequest ligneRequete : requete.getLignes()) {
            Produit produit = produitRepository.findById(ligneRequete.getProduitId())
                    .orElseThrow(() -> new RessourceIntrouvableException(
                            "Produit introuvable : id=" + ligneRequete.getProduitId()));

            if (produit.getStock() < ligneRequete.getQuantite()) {
                throw new StockInsuffisantException(
                        "Stock insuffisant pour \"" + produit.getNom() + "\" : demande="
                                + ligneRequete.getQuantite() + ", disponible=" + produit.getStock());
            }

            // Le prix est fige au moment de l'achat (copie depuis Produit),
            // pour que la commande reste correcte meme si le prix change
            // plus tard -- jamais fourni par le client dans la requete.
            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(ligneRequete.getQuantite());
            ligne.setPrixUnitaire(produit.getPrixUnitaire());
            ligne.setCommande(commande);
            commande.getLignes().add(ligne);

            produit.setStock(produit.getStock() - ligneRequete.getQuantite());
            produitRepository.save(produit);
        }

        commande.calculerMontantTotal();
        Commande enregistree = commandeRepository.save(commande);
        return versReponse(enregistree);
    }

    public List<CommandeResponse> listerCommandesAcheteur(Long acheteurId) {
        return commandeRepository.findByAcheteurId(acheteurId).stream()
                .map(this::versReponse)
                .toList();
    }

    public CommandeResponse obtenirCommande(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Commande introuvable : id=" + commandeId));
        return versReponse(commande);
    }

    @Transactional
    public CommandeResponse changerStatut(Long commandeId, ChangerStatutCommandeRequest requete) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Commande introuvable : id=" + commandeId));

        String nouveauStatut = requete.getStatut();
        if (nouveauStatut == null || !STATUTS_VALIDES.contains(nouveauStatut)) {
            throw new IllegalArgumentException(
                    "Statut invalide : \"" + nouveauStatut + "\". Valeurs acceptees : " + STATUTS_VALIDES);
        }

        commande.setStatut(nouveauStatut);
        Commande enregistree = commandeRepository.save(commande);
        return versReponse(enregistree);
    }

    private CommandeResponse versReponse(Commande commande) {
        List<LigneCommandeResponse> lignes = commande.getLignes().stream()
                .map(ligne -> new LigneCommandeResponse(
                        ligne.getProduit().getId(),
                        ligne.getProduit().getNom(),
                        ligne.getQuantite(),
                        ligne.getPrixUnitaire(),
                        ligne.getPrixUnitaire().multiply(java.math.BigDecimal.valueOf(ligne.getQuantite()))
                ))
                .toList();

        Utilisateur acheteur = commande.getAcheteur();
        return new CommandeResponse(
                commande.getId(),
                commande.getDateCommande(),
                commande.getStatut(),
                commande.getMontantTotal(),
                commande.getAdresseLivraison(),
                acheteur.getId(),
                acheteur.getNom() + " " + acheteur.getPrenom(),
                lignes
        );
    }
}
