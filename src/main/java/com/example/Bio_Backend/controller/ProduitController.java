package com.example.bio_backend.controller;

import com.example.bio_backend.dto.CreerProduitRequest;
import com.example.bio_backend.dto.ModifierProduitRequest;
import com.example.bio_backend.dto.ProduitResponse;
import com.example.bio_backend.service.ProduitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST du module Produits (catalogue des larves BSF).
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/produits   (publier -- Producteur VERIFIE uniquement)
 * {
 *   "producteurId": 1,
 *   "nom": "Larves fraiches - lot A",
 *   "etat": "FRAIS",
 *   "prixUnitaire": 500,
 *   "stock": 100
 * }
 * -> 201 CREATED si le Producteur est VERIFIE.
 * -> 403 FORBIDDEN si son compte n'est pas encore verifie.
 * -> 404 NOT FOUND si producteurId n'existe pas.
 *
 * GET http://localhost:8080/api/produits
 * -> catalogue public (uniquement les produits avec stock > 0).
 *
 * GET http://localhost:8080/api/produits/producteur/1
 * -> tous les produits de ce Producteur (y compris stock 0), pour qu'il gere
 *    son propre catalogue.
 *
 * PUT http://localhost:8080/api/produits/1
 * {
 *   "nom": "Larves fraiches - lot A",
 *   "etat": "FRAIS",
 *   "prixUnitaire": 550,
 *   "stock": 80
 * }
 *
 * DELETE http://localhost:8080/api/produits/1
 * -> 204 No Content.
 */
@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @PostMapping
    public ResponseEntity<ProduitResponse> publier(@RequestBody CreerProduitRequest requete) {
        ProduitResponse reponse = produitService.publierProduit(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping
    public ResponseEntity<List<ProduitResponse>> catalogue() {
        return ResponseEntity.ok(produitService.listerCatalogue());
    }

    @GetMapping("/producteur/{producteurId}")
    public ResponseEntity<List<ProduitResponse>> parProducteur(@PathVariable Long producteurId) {
        return ResponseEntity.ok(produitService.listerParProducteur(producteurId));
    }

    @PutMapping("/{produitId}")
    public ResponseEntity<ProduitResponse> modifier(@PathVariable Long produitId,
                                                     @RequestBody ModifierProduitRequest requete) {
        return ResponseEntity.ok(produitService.modifierProduit(produitId, requete));
    }

    @DeleteMapping("/{produitId}")
    public ResponseEntity<Void> supprimer(@PathVariable Long produitId) {
        produitService.supprimerProduit(produitId);
        return ResponseEntity.noContent().build();
    }
}
