package com.example.bio_backend.controller;

import com.example.bio_backend.dto.ConfirmerPaiementRequest;
import com.example.bio_backend.dto.CreerPaiementRequest;
import com.example.bio_backend.dto.PaiementResponse;
import com.example.bio_backend.service.PaiementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
