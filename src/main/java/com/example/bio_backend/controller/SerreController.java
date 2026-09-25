package com.example.bio_backend.controller;

import com.example.bio_backend.dto.CreerSerreRequest;
import com.example.bio_backend.dto.SerreDashboardResponse;
import com.example.bio_backend.dto.SerreResponse;
import com.example.bio_backend.service.SerreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST du module Serres (partie "configuration" de l'IoT).
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/serres   (creer une serre -- Producteur VERIFIE uniquement)
 * {
 *   "producteurId": 1,
 *   "nom": "Serre A",
 *   "localisation": "Secteur 15, Ouagadougou",
 *   "capaciteMax": 5000
 * }
 * -> 201 CREATED si le Producteur est VERIFIE.
 * -> 403 FORBIDDEN si son compte n'est pas encore verifie.
 * -> 404 NOT FOUND si producteurId n'existe pas.
 *
 * GET http://localhost:8080/api/serres/producteur/1
 * -> toutes les serres de ce Producteur.
 *
 * GET http://localhost:8080/api/serres/1
 * -> detail d'une serre.
 *
 * GET http://localhost:8080/api/serres/1/dashboard
 * -> vue temps reel : derniere mesure de chaque capteur (+ "silencieux" si
 *    aucune mesure recue depuis plus de 5 min) et alertes non traitees.
 */
@RestController
@RequestMapping("/api/serres")
public class SerreController {

    private final SerreService serreService;

    public SerreController(SerreService serreService) {
        this.serreService = serreService;
    }

    @PostMapping
    public ResponseEntity<SerreResponse> creer(@RequestBody CreerSerreRequest requete) {
        SerreResponse reponse = serreService.creerSerre(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/producteur/{producteurId}")
    public ResponseEntity<List<SerreResponse>> parProducteur(@PathVariable Long producteurId) {
        return ResponseEntity.ok(serreService.listerParProducteur(producteurId));
    }

    @GetMapping("/{serreId}")
    public ResponseEntity<SerreResponse> obtenir(@PathVariable Long serreId) {
        return ResponseEntity.ok(serreService.obtenirSerre(serreId));
    }

    @GetMapping("/{serreId}/dashboard")
    public ResponseEntity<SerreDashboardResponse> dashboard(@PathVariable Long serreId) {
        return ResponseEntity.ok(serreService.obtenirDashboard(serreId));
    }
}
