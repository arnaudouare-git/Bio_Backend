package com.example.bio_backend.controller;

import com.example.bio_backend.dto.CreerMesureRequest;
import com.example.bio_backend.dto.MesureResponse;
import com.example.bio_backend.service.MesureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST des mesures (releves envoyes par un capteur).
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/mesures   (un capteur envoie une lecture)
 * {
 *   "capteurId": 1,
 *   "temperature": 41.5,
 *   "humidite": 65
 * }
 * -> 201 CREATED. -> 404 NOT FOUND si capteurId n'existe pas.
 * -> Ici, temperature=41.5 depasse le seuil du CDC (>39C) : une Alerte
 *    "TEMPERATURE_ELEVEE" est automatiquement creee pour la serre de ce capteur
 *    (verifiable via GET /api/alertes/serre/{serreId}).
 *
 * GET http://localhost:8080/api/mesures/capteur/1
 * -> historique des 50 dernieres mesures de ce capteur (plus recentes en premier).
 */
@RestController
@RequestMapping("/api/mesures")
public class MesureController {

    private final MesureService mesureService;

    public MesureController(MesureService mesureService) {
        this.mesureService = mesureService;
    }

    @PostMapping
    public ResponseEntity<MesureResponse> enregistrer(@RequestBody CreerMesureRequest requete) {
        MesureResponse reponse = mesureService.enregistrerMesure(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/capteur/{capteurId}")
    public ResponseEntity<List<MesureResponse>> historique(@PathVariable Long capteurId) {
        return ResponseEntity.ok(mesureService.listerHistoriqueCapteur(capteurId));
    }
}
