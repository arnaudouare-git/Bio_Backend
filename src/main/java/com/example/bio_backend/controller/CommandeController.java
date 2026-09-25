package com.example.bio_backend.controller;

import com.example.bio_backend.config.UtilisateurDetailsImpl;
import com.example.bio_backend.dto.ChangerStatutCommandeRequest;
import com.example.bio_backend.dto.CommandeResponse;
import com.example.bio_backend.dto.CreerCommandeRequest;
import com.example.bio_backend.service.CommandeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST du module Commandes.
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/commandes   (passer commande -- Producteur OU Client VERIFIE)
 * Header : Authorization: Bearer <token>
 * {
 *   "adresseLivraison": "Secteur 15, Ouagadougou",
 *   "lignes": [
 *     { "produitId": 1, "quantite": 10 }
 *   ]
 * }
 * -> 201 CREATED si l'acheteur (deduit du token, plus de "acheteurId" dans
 *    le body depuis le 2026-09-23) est VERIFIE et le stock suffisant.
 * -> 403 FORBIDDEN si l'acheteur n'est pas encore verifie.
 * -> 409 CONFLICT si le stock demande depasse le stock disponible.
 * -> 404 NOT FOUND si un produitId n'existe pas.
 *
 * GET http://localhost:8080/api/commandes/acheteur/2
 * -> toutes les commandes de cet acheteur.
 * -> 403 FORBIDDEN si l'appelant (via son token) n'est ni cet acheteur, ni
 *    un Administrateur (verification cablee le 2026-09-23).
 *
 * GET http://localhost:8080/api/commandes/1
 * -> le detail d'une commande (lignes, montant total...).
 * -> 403 FORBIDDEN si l'appelant n'est ni l'acheteur de cette commande, ni
 *    un Administrateur.
 *
 * PUT http://localhost:8080/api/commandes/1/statut   (reserve a l'ACHETEUR et a l'ADMIN)
 * {
 *   "statut": "ANNULEE"
 * }
 * -> un ACHETEUR ne peut qu'annuler (statut="ANNULEE"), et seulement si
 *    aucun producteur n'a encore commence a traiter sa ligne ET que la
 *    commande a moins de 12h (delai de retractation, ajoute le 2026-09-24).
 * -> un ADMINISTRATEUR peut forcer n'importe quel statut ; cela s'applique
 *    alors a la commande ET a toutes ses lignes (vue d'ensemble).
 * -> 403 FORBIDDEN si l'appelant n'est ni l'acheteur, ni un Administrateur
 *    (un Producteur doit utiliser la route ci-dessous, par ligne).
 *
 * PUT http://localhost:8080/api/commandes/1/lignes/3/statut   (reserve au PRODUCTEUR proprietaire de la ligne, et a l'ADMIN)
 * {
 *   "statut": "EXPEDIEE"
 * }
 * -> chaque Producteur fait avancer UNIQUEMENT les lignes de ses propres
 *    produits (ligneId, visible dans la reponse d'une commande) --
 *    n'affecte jamais les lignes des autres Producteurs dans la meme
 *    commande, meme si elle est multi-producteurs (ajoute le 2026-09-23).
 * -> valeurs acceptees : EN_ATTENTE, CONFIRMEE, EN_PREPARATION, EXPEDIEE, LIVREE, ANNULEE, REFUSEE.
 * -> un Producteur ne peut qu'avancer dans l'ordre (EN_ATTENTE -> CONFIRMEE
 *    -> EN_PREPARATION -> EXPEDIEE -> LIVREE), jamais reculer ni sauter en
 *    arriere ; ANNULEE reste possible tant que la ligne n'est pas deja
 *    LIVREE ou ANNULEE. Un Administrateur n'a aucune de ces restrictions.
 * -> 403 FORBIDDEN si l'appelant ne possede pas cette ligne (et n'est pas Admin).
 * -> 400 BAD REQUEST si la transition demandee n'est pas valide.
 *
 * PUT http://localhost:8080/api/commandes/1/lignes/3/statut   (refus -- ajoute le 2026-09-24, Tier 1)
 * {
 *   "statut": "REFUSEE",
 *   "motif": "Stock reserve entre-temps pour un autre acheteur"
 * }
 * -> reserve au Producteur proprietaire de la ligne (tant qu'elle est encore
 *    EN_ATTENTE) ou a l'Administrateur (a tout moment). "motif" obligatoire.
 * -> restitue automatiquement le stock decremente au moment de la commande.
 * -> 400 BAD REQUEST si le motif est manquant, si la ligne est deja
 *    REFUSEE, ou si elle n'est plus EN_ATTENTE (et l'appelant n'est pas Admin).
 */
@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @PostMapping
    public ResponseEntity<CommandeResponse> passerCommande(@RequestBody CreerCommandeRequest requete,
                                                             @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        CommandeResponse reponse = commandeService.passerCommande(requete, principal.getUtilisateur());
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/acheteur/{acheteurId}")
    public ResponseEntity<List<CommandeResponse>> listerParAcheteur(@PathVariable Long acheteurId,
                                                                     @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        return ResponseEntity.ok(commandeService.listerCommandesAcheteur(acheteurId, principal.getUtilisateur()));
    }

    @GetMapping("/{commandeId}")
    public ResponseEntity<CommandeResponse> obtenir(@PathVariable Long commandeId,
                                                      @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        return ResponseEntity.ok(commandeService.obtenirCommande(commandeId, principal.getUtilisateur()));
    }

    @PutMapping("/{commandeId}/statut")
    public ResponseEntity<CommandeResponse> changerStatut(@PathVariable Long commandeId,
                                                           @RequestBody ChangerStatutCommandeRequest requete,
                                                           @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        return ResponseEntity.ok(commandeService.changerStatut(commandeId, requete, principal.getUtilisateur()));
    }

    @PutMapping("/{commandeId}/lignes/{ligneId}/statut")
    public ResponseEntity<CommandeResponse> changerStatutLigne(@PathVariable Long commandeId,
                                                                @PathVariable Long ligneId,
                                                                @RequestBody ChangerStatutCommandeRequest requete,
                                                                @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        return ResponseEntity.ok(commandeService.changerStatutLigne(
                commandeId, ligneId, requete, principal.getUtilisateur()));
    }
}
