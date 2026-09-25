package com.example.bio_backend.controller;

import com.example.bio_backend.dto.ActionneurResponse;
import com.example.bio_backend.dto.ChangerEtatActionneurRequest;
import com.example.bio_backend.service.ActionneurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST du module Actionneur (ajoute le 2026-09-24, retour client
 * en entretien : "pilotage automatique pour arroser la serre").
 *
 * Testable avec Postman :
 *
 * GET http://localhost:8080/api/actionneurs/serre/1
 * -> tous les actionneurs de cette serre (lecture publique, comme le reste
 *    du monitoring IoT). Aujourd'hui un seul actionneur possible par serre,
 *    type "IRRIGATION", cree automatiquement a la premiere mesure
 *    d'humidite recue pour cette serre.
 * -> son etat ("ACTIF"/"INACTIF") change automatiquement : ACTIF si la
 *    derniere mesure d'humidite est sous le seuil HUMIDITE_BASSE du CDC
 *    (50%, voir MesureService), INACTIF sinon.
 *
 * PUT http://localhost:8080/api/actionneurs/3/etat   (reserve Producteur/Admin)
 * { "etat": "ACTIF" }
 * -> pilotage manuel, peut etre ecrase par la prochaine mesure automatique.
 * -> 400 BAD REQUEST si "etat" n'est pas "ACTIF" ou "INACTIF".
 */
@RestController
@RequestMapping("/api/actionneurs")
public class ActionneurController {

    private final ActionneurService actionneurService;

    public ActionneurController(ActionneurService actionneurService) {
        this.actionneurService = actionneurService;
    }

    @GetMapping("/serre/{serreId}")
    public ResponseEntity<List<ActionneurResponse>> listerParSerre(@PathVariable Long serreId) {
        return ResponseEntity.ok(actionneurService.listerActionneursSerre(serreId));
    }

    @PutMapping("/{actionneurId}/etat")
    public ResponseEntity<ActionneurResponse> changerEtatManuel(@PathVariable Long actionneurId,
                                                                  @RequestBody ChangerEtatActionneurRequest requete) {
        return ResponseEntity.ok(actionneurService.changerEtatManuel(actionneurId, requete.getEtat()));
    }
}
