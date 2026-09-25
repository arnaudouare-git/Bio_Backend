package com.example.bio_backend.service;

import com.example.bio_backend.dto.AvisResponse;
import com.example.bio_backend.dto.CreerAvisRequest;
import com.example.bio_backend.entity.Avis;
import com.example.bio_backend.entity.Commande;
import com.example.bio_backend.entity.Producteur;
import com.example.bio_backend.entity.Utilisateur;
import com.example.bio_backend.exception.AvisDejaDonneException;
import com.example.bio_backend.exception.CommandeAccesRefuseException;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.repository.AvisRepository;
import com.example.bio_backend.repository.CommandeRepository;
import com.example.bio_backend.repository.ProducteurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logique metier du module Avis (ajoute le 2026-09-24, priorite Tier 1) :
 * note de 1 a 5 + commentaire optionnel laisse par un acheteur sur un
 * Producteur, apres reception effective d'une commande livree.
 *
 * Regle metier centrale : un avis doit toujours etre rattache a une VRAIE
 * commande de l'auteur, avec au moins une ligne LIVREE provenant de ce
 * producteur -- impossible de noter un producteur sans avoir reellement
 * achete et recu ses produits, et impossible de laisser deux avis pour la
 * meme commande.
 */
@Service
public class AvisService {

    private final AvisRepository avisRepository;
    private final CommandeRepository commandeRepository;
    private final ProducteurRepository producteurRepository;

    public AvisService(AvisRepository avisRepository,
                        CommandeRepository commandeRepository,
                        ProducteurRepository producteurRepository) {
        this.avisRepository = avisRepository;
        this.commandeRepository = commandeRepository;
        this.producteurRepository = producteurRepository;
    }

    @Transactional
    public AvisResponse laisserAvis(CreerAvisRequest requete, Utilisateur demandeur) {
        if (requete.getNote() == null || requete.getNote() < 1 || requete.getNote() > 5) {
            throw new IllegalArgumentException("La note doit etre comprise entre 1 et 5.");
        }

        Commande commande = commandeRepository.findById(requete.getCommandeId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Commande introuvable : id=" + requete.getCommandeId()));

        if (!commande.getAcheteur().getId().equals(demandeur.getId())) {
            throw new CommandeAccesRefuseException(
                    "Vous ne pouvez laisser un avis que sur vos propres commandes.");
        }

        Producteur producteur = producteurRepository.findById(requete.getProducteurId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Producteur introuvable : id=" + requete.getProducteurId()));

        boolean commandeLivreeDeProducteur = commande.getLignes().stream()
                .anyMatch(ligne -> ligne.getProduit().getProducteur().getId().equals(producteur.getId())
                        && "LIVREE".equals(ligne.getStatut()));
        if (!commandeLivreeDeProducteur) {
            throw new IllegalArgumentException(
                    "Vous ne pouvez noter ce producteur que si au moins une ligne de cette "
                            + "commande, provenant de lui, a ete livree.");
        }

        if (avisRepository.existsByAuteurIdAndCommandeIdAndProducteurId(
                demandeur.getId(), commande.getId(), producteur.getId())) {
            throw new AvisDejaDonneException(
                    "Vous avez deja laisse un avis sur ce producteur pour cette commande.");
        }

        Avis avis = new Avis();
        avis.setNote(requete.getNote());
        avis.setCommentaire(requete.getCommentaire());
        avis.setProducteur(producteur);
        avis.setAuteur(demandeur);
        avis.setCommande(commande);

        Avis enregistre = avisRepository.save(avis);
        return versReponse(enregistre);
    }

    /** Lecture publique, comme le catalogue -- tout le monde peut consulter les avis d'un Producteur. */
    public List<AvisResponse> listerAvisProducteur(Long producteurId) {
        return avisRepository.findByProducteurId(producteurId).stream()
                .map(this::versReponse)
                .toList();
    }

    private AvisResponse versReponse(Avis avis) {
        return new AvisResponse(
                avis.getId(),
                avis.getNote(),
                avis.getCommentaire(),
                avis.getAuteur().getId(),
                avis.getAuteur().getNom() + " " + avis.getAuteur().getPrenom(),
                avis.getProducteur().getId(),
                avis.getDateCreation()
        );
    }
}
