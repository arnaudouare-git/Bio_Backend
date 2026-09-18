package com.example.bio_backend.controller;

import com.example.bio_backend.dto.ChangerStatutCommandeRequest;
import com.example.bio_backend.dto.CommandeResponse;
import com.example.bio_backend.dto.CreerCommandeRequest;
import com.example.bio_backend.service.CommandeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST du module Commandes.
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/commandes   (passer commande -- Producteur OU Client VERIFIE)
 * {
 *   "acheteurId": 2,
 *   "adresseLivraison": "Secteur 15, Ouagadougou",
 *   "lignes": [
 *     { "produitId": 1, "quantite": 10 }
 *   ]
 * }
 * -> 201 CREATED si l'acheteur est VERIFIE et le stock suffisant.
 * -> 403 FORBIDDEN si l'acheteur n'est pas encore verifie.
 * -> 409 CONFLICT si le stock demande depasse le stock disponible.
 * -> 404 NOT FOUND si acheteurId ou un produitId n'existe pas.
 *
 * GET http://localhost:8080/api/commandes/acheteur/2
 * -> toutes les commandes de cet acheteur.
 *
 * GET http://localhost:8080/api/commandes/1
 * -> le detail d'une commande (lignes, montant total...).
 *
 * PUT http://localhost:8080/api/commandes/1/statut
 * {
 *   "statut": "CONFIRMEE"
 * }
 * -> valeurs acceptees : EN_ATTENTE, CONFIRMEE, EN_PREPARATION, EXPEDIEE, LIVREE, ANNULEE.
 */
@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @PostMapping
    public ResponseEntity<CommandeResponse> passerCommande(@RequestBody CreerCommandeRequest requete) {
        CommandeResponse reponse = commandeService.passerCommande(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/acheteur/{acheteurId}")
    public ResponseEntity<List<CommandeResponse>> listerParAcheteur(@PathVariable Long acheteurId) {
        return ResponseEntity.ok(commandeService.listerCommandesAcheteur(acheteurId));
    }

    @GetMapping("/{commandeId}")
    public ResponseEntity<CommandeResponse> obtenir(@PathVariable Long commandeId) {
        return ResponseEntity.ok(commandeService.obtenirCommande(commandeId));
    }

    @PutMapping("/{commandeId}/statut")
    public ResponseEntity<CommandeResponse> changerStatut(@PathVariable Long commandeId,
                                                           @RequestBody ChangerStatutCommandeRequest requete) {
        return ResponseEntity.ok(commandeService.changerStatut(commandeId, requete));
    }
}
