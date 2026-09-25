package com.example.bio_backend.service;

import com.example.bio_backend.dto.CreerLitigeRequest;
import com.example.bio_backend.dto.LitigeResponse;
import com.example.bio_backend.dto.ResoudreLitigeRequest;
import com.example.bio_backend.entity.Administrateur;
import com.example.bio_backend.entity.Commande;
import com.example.bio_backend.entity.Litige;
import com.example.bio_backend.entity.Utilisateur;
import com.example.bio_backend.exception.CommandeAccesRefuseException;
import com.example.bio_backend.exception.LitigeDejaOuvertException;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.repository.CommandeRepository;
import com.example.bio_backend.repository.LitigeRepository;
import com.example.bio_backend.repository.PaiementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Logique metier du module Litige (ajoute le 2026-09-24, priorite Tier 2) :
 * permet a un acheteur de signaler une commande PAYEE mais PAS ENCORE
 * LIVREE, pour arbitrage par un Administrateur. Ne modifie jamais lui-meme
 * le statut de la Commande -- l'admin garde la main via l'endpoint existant
 * PUT /api/commandes/{id}/statut pour tirer les consequences concretes
 * (remboursement = annulation, confirmation de livraison = LIVREE...).
 */
@Service
public class LitigeService {

    private static final Set<String> STATUTS_RESOLUTION = Set.of("RESOLU", "REJETE");

    private final LitigeRepository litigeRepository;
    private final CommandeRepository commandeRepository;
    private final PaiementRepository paiementRepository;

    public LitigeService(LitigeRepository litigeRepository,
                          CommandeRepository commandeRepository,
                          PaiementRepository paiementRepository) {
        this.litigeRepository = litigeRepository;
        this.commandeRepository = commandeRepository;
        this.paiementRepository = paiementRepository;
    }

    @Transactional
    public LitigeResponse ouvrirLitige(CreerLitigeRequest requete, Utilisateur demandeur) {
        if (requete.getMotif() == null || requete.getMotif().isBlank()) {
            throw new IllegalArgumentException("Un motif est obligatoire pour ouvrir un litige.");
        }

        Commande commande = commandeRepository.findById(requete.getCommandeId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Commande introuvable : id=" + requete.getCommandeId()));

        if (!commande.getAcheteur().getId().equals(demandeur.getId())) {
            throw new CommandeAccesRefuseException(
                    "Vous ne pouvez ouvrir un litige que sur vos propres commandes.");
        }

        boolean payee = paiementRepository.findByCommandeId(commande.getId()).stream()
                .anyMatch(p -> "REUSSI".equals(p.getStatut()));
        if (!payee) {
            throw new IllegalArgumentException(
                    "Impossible d'ouvrir un litige sur une commande qui n'a pas encore ete payee avec succes.");
        }

        if ("LIVREE".equals(commande.getStatut())) {
            throw new IllegalArgumentException(
                    "Cette commande est deja marquee comme livree ; un litige ne s'applique qu'a "
                            + "une commande payee et non (encore) livree.");
        }

        if (litigeRepository.existsByCommandeIdAndStatut(commande.getId(), "OUVERT")) {
            throw new LitigeDejaOuvertException(
                    "Un litige est deja ouvert et en attente d'arbitrage pour cette commande.");
        }

        Litige litige = new Litige();
        litige.setCommande(commande);
        litige.setAuteur(demandeur);
        litige.setMotif(requete.getMotif());

        Litige enregistre = litigeRepository.save(litige);
        return versReponse(enregistre);
    }

    /** Reserve a l'Administrateur (voir SecurityConfig) : vue d'ensemble pour le triage. */
    public List<LitigeResponse> listerTousLitiges() {
        return litigeRepository.findAll().stream()
                .map(this::versReponse)
                .toList();
    }

    /** L'auteur du litige ou un Administrateur seulement. */
    public LitigeResponse obtenirLitige(Long litigeId, Utilisateur demandeur) {
        Litige litige = litigeRepository.findById(litigeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Litige introuvable : id=" + litigeId));
        verifierAuteurOuAdmin(litige, demandeur);
        return versReponse(litige);
    }

    /** Meme regle de propriete que listerCommandesAcheteur() dans CommandeService. */
    public List<LitigeResponse> listerLitigesUtilisateur(Long utilisateurId, Utilisateur demandeur) {
        if (!(demandeur instanceof Administrateur) && !utilisateurId.equals(demandeur.getId())) {
            throw new CommandeAccesRefuseException("Vous ne pouvez consulter que vos propres litiges.");
        }
        return litigeRepository.findByAuteurId(utilisateurId).stream()
                .map(this::versReponse)
                .toList();
    }

    /** Reserve a l'Administrateur (voir SecurityConfig) : tranche un litige OUVERT. */
    @Transactional
    public LitigeResponse resoudreLitige(Long litigeId, ResoudreLitigeRequest requete) {
        Litige litige = litigeRepository.findById(litigeId)
                .orElseThrow(() -> new RessourceIntrouvableException("Litige introuvable : id=" + litigeId));

        String nouveauStatut = requete.getStatut();
        if (nouveauStatut == null || !STATUTS_RESOLUTION.contains(nouveauStatut)) {
            throw new IllegalArgumentException(
                    "Statut de resolution invalide : \"" + nouveauStatut + "\". Valeurs acceptees : "
                            + STATUTS_RESOLUTION);
        }
        if (!"OUVERT".equals(litige.getStatut())) {
            throw new IllegalArgumentException(
                    "Ce litige a deja ete tranche (statut actuel : \"" + litige.getStatut() + "\").");
        }
        if (requete.getDecisionAdmin() == null || requete.getDecisionAdmin().isBlank()) {
            throw new IllegalArgumentException(
                    "Une decision (justification) est obligatoire pour trancher un litige.");
        }

        litige.setStatut(nouveauStatut);
        litige.setDecisionAdmin(requete.getDecisionAdmin());
        litige.setDateResolution(LocalDateTime.now());

        Litige enregistre = litigeRepository.save(litige);
        return versReponse(enregistre);
    }

    private void verifierAuteurOuAdmin(Litige litige, Utilisateur demandeur) {
        if (demandeur instanceof Administrateur) {
            return;
        }
        if (!litige.getAuteur().getId().equals(demandeur.getId())) {
            throw new CommandeAccesRefuseException("Vous ne pouvez consulter que vos propres litiges.");
        }
    }

    private LitigeResponse versReponse(Litige litige) {
        Utilisateur auteur = litige.getAuteur();
        return new LitigeResponse(
                litige.getId(),
                litige.getCommande().getId(),
                auteur.getId(),
                auteur.getNom() + " " + auteur.getPrenom(),
                litige.getMotif(),
                litige.getStatut(),
                litige.getDecisionAdmin(),
                litige.getDateCreation(),
                litige.getDateResolution()
        );
    }
}
