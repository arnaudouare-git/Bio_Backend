package com.example.bio_backend.controller;

import com.example.bio_backend.config.UtilisateurDetailsImpl;
import com.example.bio_backend.dto.AvisResponse;
import com.example.bio_backend.dto.CreerAvisRequest;
import com.example.bio_backend.service.AvisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST du module Avis (ajoute le 2026-09-24, priorite Tier 1) :
 * note de 1 a 5 + commentaire optionnel sur un Producteur.
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/avis
 * Header : Authorization: Bearer <token>
 * {
 *   "commandeId": 1,
 *   "producteurId": 3,
 *   "note": 5,
 *   "commentaire": "Livraison rapide, larves de tres bonne qualite."
 * }
 * -> 201 CREATED si l'appelant est bien l'acheteur de cette commande ET
 *    qu'au moins une ligne provenant de ce producteur y est LIVREE.
 * -> 400 BAD REQUEST si la note n'est pas entre 1 et 5, ou si aucune ligne
 *    livree de ce producteur n'existe dans cette commande.
 * -> 403 FORBIDDEN si l'appelant n'est pas l'acheteur de la commande.
 * -> 404 NOT FOUND si commandeId ou producteurId n'existe pas.
 * -> 409 CONFLICT si un avis existe deja pour ce (acheteur, commande, producteur).
 *
 * GET http://localhost:8080/api/avis/producteur/3
 * -> tous les avis recus par ce Producteur (lecture publique, comme le catalogue).
 */
@RestController
@RequestMapping("/api/avis")
public class AvisController {

    private final AvisService avisService;

    public AvisController(AvisService avisService) {
        this.avisService = avisService;
    }

    @PostMapping
    public ResponseEntity<AvisResponse> laisserAvis(@RequestBody CreerAvisRequest requete,
                                                      @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        AvisResponse reponse = avisService.laisserAvis(requete, principal.getUtilisateur());
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/producteur/{producteurId}")
    public ResponseEntity<List<AvisResponse>> parProducteur(@PathVariable Long producteurId) {
        return ResponseEntity.ok(avisService.listerAvisProducteur(producteurId));
    }
}
