package com.example.bio_backend.controller;

import com.example.bio_backend.config.UtilisateurDetailsImpl;
import com.example.bio_backend.dto.CreerLitigeRequest;
import com.example.bio_backend.dto.LitigeResponse;
import com.example.bio_backend.dto.ResoudreLitigeRequest;
import com.example.bio_backend.service.LitigeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST du module Litige (ajoute le 2026-09-24, priorite Tier 2) :
 * une commande PAYEE mais PAS ENCORE LIVREE peut etre signalee par son
 * acheteur pour arbitrage par un Administrateur.
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/litiges   (n'importe quel compte connecte, sur SA PROPRE commande)
 * Header : Authorization: Bearer <token acheteur>
 * {
 *   "commandeId": 7,
 *   "motif": "Commande payee il y a 5 jours, toujours pas expediee."
 * }
 * -> 201 CREATED, statut "OUVERT".
 * -> 403 FORBIDDEN si l'appelant n'est pas l'acheteur de cette commande.
 * -> 400 BAD REQUEST si la commande n'a pas de paiement REUSSI, ou si elle est deja LIVREE.
 * -> 409 CONFLICT si un litige OUVERT existe deja pour cette commande.
 *
 * GET http://localhost:8080/api/litiges   (reserve Administrateur)
 * -> tous les litiges de la plateforme, pour triage.
 *
 * GET http://localhost:8080/api/litiges/3
 * -> le detail d'un litige. Reserve a son auteur ou a un Administrateur (403 sinon).
 *
 * GET http://localhost:8080/api/litiges/utilisateur/12
 * -> tous les litiges ouverts par cet utilisateur. Reserve a lui-meme ou a un Administrateur.
 *
 * PUT http://localhost:8080/api/litiges/3/statut   (reserve Administrateur)
 * {
 *   "statut": "RESOLU",
 *   "decisionAdmin": "Remboursement effectue manuellement ; commande annulee via /api/commandes/7/statut."
 * }
 * -> valeurs acceptees pour "statut" : RESOLU, REJETE. "decisionAdmin" obligatoire.
 * -> 400 BAD REQUEST si le litige n'est plus OUVERT (deja tranche).
 * -> Ne modifie PAS automatiquement le statut de la commande : utiliser
 *    PUT /api/commandes/{id}/statut separement si une action concrete est necessaire.
 */
@RestController
@RequestMapping("/api/litiges")
public class LitigeController {

    private final LitigeService litigeService;

    public LitigeController(LitigeService litigeService) {
        this.litigeService = litigeService;
    }

    @PostMapping
    public ResponseEntity<LitigeResponse> ouvrirLitige(@RequestBody CreerLitigeRequest requete,
                                                         @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        LitigeResponse reponse = litigeService.ouvrirLitige(requete, principal.getUtilisateur());
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping
    public ResponseEntity<List<LitigeResponse>> listerTous() {
        return ResponseEntity.ok(litigeService.listerTousLitiges());
    }

    @GetMapping("/{litigeId}")
    public ResponseEntity<LitigeResponse> obtenir(@PathVariable Long litigeId,
                                                   @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        return ResponseEntity.ok(litigeService.obtenirLitige(litigeId, principal.getUtilisateur()));
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<LitigeResponse>> listerParUtilisateur(@PathVariable Long utilisateurId,
                                                                      @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        return ResponseEntity.ok(litigeService.listerLitigesUtilisateur(utilisateurId, principal.getUtilisateur()));
    }

    @PutMapping("/{litigeId}/statut")
    public ResponseEntity<LitigeResponse> resoudre(@PathVariable Long litigeId,
                                                    @RequestBody ResoudreLitigeRequest requete) {
        return ResponseEntity.ok(litigeService.resoudreLitige(litigeId, requete));
    }
}
