package com.example.bio_backend.service;

import com.example.bio_backend.dto.CreerProduitRequest;
import com.example.bio_backend.dto.ModifierProduitRequest;
import com.example.bio_backend.dto.ProduitResponse;
import com.example.bio_backend.entity.Produit;
import com.example.bio_backend.entity.Producteur;
import com.example.bio_backend.exception.CompteNonVerifieException;
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

    @Transactional
    public ProduitResponse modifierProduit(Long produitId, ModifierProduitRequest requete) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Produit introuvable : id=" + produitId));

        produit.setNom(requete.getNom());
        produit.setEtat(requete.getEtat());
        produit.setPrixUnitaire(requete.getPrixUnitaire());
        produit.setStock(requete.getStock());

        Produit enregistre = produitRepository.save(produit);
        return versReponse(enregistre);
    }

    @Transactional
    public void supprimerProduit(Long produitId) {
        if (!produitRepository.existsById(produitId)) {
            throw new RessourceIntrouvableException("Produit introuvable : id=" + produitId);
        }
        produitRepository.deleteById(produitId);
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
