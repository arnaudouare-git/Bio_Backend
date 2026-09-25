package com.example.bio_backend.service;

import com.example.bio_backend.dto.CreerProduitRequest;
import com.example.bio_backend.dto.ModifierProduitRequest;
import com.example.bio_backend.dto.ProduitResponse;
import com.example.bio_backend.entity.Administrateur;
import com.example.bio_backend.entity.Produit;
import com.example.bio_backend.entity.Producteur;
import com.example.bio_backend.entity.Utilisateur;
import com.example.bio_backend.exception.CompteNonVerifieException;
import com.example.bio_backend.exception.ProduitNonProprietaireException;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.repository.ProducteurRepository;
import com.example.bio_backend.repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logique metier du catalogue de produits (lots de larves BSF en vente).
 *
 * Regle metier centrale, cablee ici pour la premiere fois (elle etait notee
 * comme dette technique depuis le module Authentification) : seul un
 * Producteur dont le compte est VERIFIE (formation + CNIB valides par un
 * Administrateur, voir AdminService.activerCompte) peut publier un produit.
 * Vendre reste un privilege exclusif du Producteur (jamais du Client), et
 * meme pour lui, jamais avant activation du compte.
 */
@Service
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final ProducteurRepository producteurRepository;

    public ProduitService(ProduitRepository produitRepository, ProducteurRepository producteurRepository) {
        this.produitRepository = produitRepository;
        this.producteurRepository = producteurRepository;
    }

    @Transactional
    public ProduitResponse publierProduit(CreerProduitRequest requete) {
        Producteur producteur = producteurRepository.findById(requete.getProducteurId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Producteur introuvable : id=" + requete.getProducteurId()));

        if (!"VERIFIE".equals(producteur.getStatutVerificationCnib())) {
            throw new CompteNonVerifieException(
                    "Impossible de publier un produit : ce compte n'est pas encore verifie "
                            + "(formation + verification de la piece d'identite requises).");
        }

        Produit produit = new Produit();
        produit.setNom(requete.getNom());
        produit.setEtat(requete.getEtat());
        produit.setPrixUnitaire(requete.getPrixUnitaire());
        produit.setStock(requete.getStock());
        produit.setProducteur(producteur);

        Produit enregistre = produitRepository.save(produit);
        return versReponse(enregistre);
    }

    /**
     * Catalogue public : uniquement les produits encore en stock (stock > 0).
     * C'est cette liste que Producteur ET Client (tout acheteur) consultent
     * pour passer commande.
     */
    public List<ProduitResponse> listerCatalogue() {
        return produitRepository.findByStockGreaterThan(0).stream()
                .map(this::versReponse)
                .toList();
    }

    /**
     * Vue "Gerer mon catalogue" d'un Producteur : TOUS ses produits, y
     * compris ceux a stock 0 (qu'il doit pouvoir reapprovisionner), au
     * contraire du catalogue public.
     */
    public List<ProduitResponse> listerParProducteur(Long producteurId) {
        return produitRepository.findByProducteurId(producteurId).stream()
                .map(this::versReponse)
                .toList();
    }

    /**
     * Module Recherche geolocalisee (ajoute le 2026-09-24, priorite Tier 1) :
     * catalogue public filtre par distance a un point de recherche
     * (latitude/longitude, ex : la position du Client), triee du plus proche
     * au plus loin. Les produits dont le Producteur n'a pas encore renseigne
     * de latitude/longitude sont ignores (rien a calculer). Distance a vol
     * d'oiseau via la formule de Haversine -- suffisant pour un filtre de
     * proximite sans extension PostgreSQL type PostGIS (voir le commentaire
     * historique dans ProducteurRepository).
     */
    public List<ProduitResponse> rechercherProduitsProximite(double latitude, double longitude, double rayonKm) {
        return produitRepository.findByStockGreaterThan(0).stream()
                .filter(produit -> produit.getProducteur().getLatitude() != null
                        && produit.getProducteur().getLongitude() != null)
                .map(produit -> {
                    double distance = calculerDistanceKm(latitude, longitude,
                            produit.getProducteur().getLatitude(), produit.getProducteur().getLongitude());
                    ProduitResponse reponse = versReponse(produit);
                    reponse.setDistanceKm(Math.round(distance * 100.0) / 100.0);
                    return reponse;
                })
                .filter(reponse -> reponse.getDistanceKm() <= rayonKm)
                .sorted(java.util.Comparator.comparingDouble(ProduitResponse::getDistanceKm))
                .toList();
    }

    /**
     * Formule de Haversine : distance approximative (en km) "a vol d'oiseau"
     * entre deux points GPS donnes en degres decimaux.
     */
    private double calculerDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double rayonTerreKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return rayonTerreKm * c;
    }

    /**
     * Modifie un produit existant.
     *
     * "demandeur" est l'utilisateur authentifie via le token JWT de la
     * requete (voir ProduitController), pas une valeur envoyee dans le
     * body -- impossible donc de se faire passer pour quelqu'un d'autre.
     * Un Producteur ne peut modifier que SES PROPRES produits ; un
     * Administrateur peut modifier n'importe lequel (moderation).
     */
    @Transactional
    public ProduitResponse modifierProduit(Long produitId, ModifierProduitRequest requete, Utilisateur demandeur) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Produit introuvable : id=" + produitId));

        verifierProprietaireOuAdmin(produit, demandeur);

        produit.setNom(requete.getNom());
        produit.setEtat(requete.getEtat());
        produit.setPrixUnitaire(requete.getPrixUnitaire());
        produit.setStock(requete.getStock());

        Produit enregistre = produitRepository.save(produit);
        return versReponse(enregistre);
    }

    /** Meme regle de propriete que modifierProduit() ci-dessus -- voir sa javadoc. */
    @Transactional
    public void supprimerProduit(Long produitId, Utilisateur demandeur) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RessourceIntrouvableException("Produit introuvable : id=" + produitId));

        verifierProprietaireOuAdmin(produit, demandeur);

        produitRepository.delete(produit);
    }

    /**
     * Controle d'acces cable le 2026-09-22, une fois le module Securite
     * (JWT) disponible : avant, n'importe quel appelant pouvait modifier ou
     * supprimer n'importe quel produit simplement en connaissant son id
     * (dette technique notee depuis l'ecriture initiale du module
     * Produits). Desormais, on compare le proprietaire reel du produit en
     * base a l'utilisateur authentifie de la requete.
     */
    private void verifierProprietaireOuAdmin(Produit produit, Utilisateur demandeur) {
        if (demandeur instanceof Administrateur) {
            return;
        }
        if (!produit.getProducteur().getId().equals(demandeur.getId())) {
            throw new ProduitNonProprietaireException(
                    "Vous ne pouvez modifier ou supprimer que vos propres produits.");
        }
    }

    private ProduitResponse versReponse(Produit produit) {
        return new ProduitResponse(
                produit.getId(),
                produit.getNom(),
                produit.getEtat(),
                produit.getPrixUnitaire(),
                produit.getStock(),
                produit.getProducteur().getId(),
                produit.getProducteur().getNom() + " " + produit.getProducteur().getPrenom()
        );
    }
}
