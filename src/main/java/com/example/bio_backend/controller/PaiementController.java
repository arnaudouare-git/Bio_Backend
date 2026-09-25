package com.example.bio_backend.controller;

import com.example.bio_backend.config.UtilisateurDetailsImpl;
import com.example.bio_backend.dto.ConfirmerPaiementRequest;
import com.example.bio_backend.dto.CreerPaiementRequest;
import com.example.bio_backend.dto.PaiementResponse;
import com.example.bio_backend.service.PaiementService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST du module Paiements (simulation Orange Money + especes).
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/paiements   (initier un paiement)
 * {
 *   "commandeId": 4,
 *   "methode": "ORANGE_MONEY"
 * }
 * -> 201 CREATED, statut "EN_ATTENTE", une referenceTransaction "OM-XXXXXXXX" est generee.
 * -> methode peut aussi valoir "CASH" (pas de referenceTransaction, pas de confirmation possible).
 * -> 404 NOT FOUND si commandeId n'existe pas.
 * -> 409 CONFLICT si la commande a deja un paiement REUSSI.
 *
 * PUT http://localhost:8080/api/paiements/1/confirmer   (simule le retour d'Orange Money)
 * {
 *   "reussi": true
 * }
 * -> 200 OK, statut passe a "REUSSI" et la commande passe automatiquement a "CONFIRMEE".
 * -> "reussi": false -> statut passe a "ECHOUE" (la commande n'est pas confirmee).
 * -> 409 CONFLICT si ce paiement a deja ete confirme/echoue.
 * -> 400 BAD REQUEST si le paiement est en CASH (rien a confirmer via l'API).
 *
 * GET http://localhost:8080/api/paiements/commande/4
 * -> tous les paiements (tentatives) lies a cette commande.
 *
 * GET http://localhost:8080/api/paiements/1/facture   (ajoute le 2026-09-24, Tier 2)
 * Header : Authorization: Bearer <token acheteur ou admin>
 * -> telecharge la facture PDF de la commande liee a ce paiement.
 * -> 403 FORBIDDEN si l'appelant n'est ni l'acheteur, ni un Administrateur.
 * -> 400 BAD REQUEST si ce paiement n'a pas (encore) reussi.
 */
@RestController
@RequestMapping("/api/paiements")
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @PostMapping
    public ResponseEntity<PaiementResponse> initierPaiement(@RequestBody CreerPaiementRequest requete) {
        PaiementResponse reponse = paiementService.initierPaiement(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @PutMapping("/{paiementId}/confirmer")
    public ResponseEntity<PaiementResponse> confirmerPaiement(@PathVariable Long paiementId,
                                                               @RequestBody ConfirmerPaiementRequest requete) {
        return ResponseEntity.ok(paiementService.confirmerPaiement(paiementId, requete));
    }

    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<List<PaiementResponse>> listerParCommande(@PathVariable Long commandeId) {
        return ResponseEntity.ok(paiementService.listerPaiementsCommande(commandeId));
    }

    @GetMapping("/{paiementId}/facture")
    public ResponseEntity<byte[]> genererFacture(@PathVariable Long paiementId,
                                                  @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        byte[] pdf = paiementService.genererFacturePdf(paiementId, principal.getUtilisateur());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"facture-paiement-" + paiementId + ".pdf\"")
                .body(pdf);
    }
}
