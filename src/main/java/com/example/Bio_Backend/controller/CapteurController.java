package com.example.bio_backend.controller;

import com.example.bio_backend.dto.CapteurResponse;
import com.example.bio_backend.dto.CreerCapteurRequest;
import com.example.bio_backend.service.SerreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST des capteurs (rattaches a une serre).
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/capteurs   (installer un capteur dans une serre)
 * {
 *   "serreId": 1,
 *   "type": "TEMPERATURE_HUMIDITE"
 * }
 * -> 201 CREATED. -> 404 NOT FOUND si serreId n'existe pas.
 *
 * GET http://localhost:8080/api/capteurs/serre/1
 * -> tous les capteurs installes dans cette serre.
 */
@RestController
@RequestMapping("/api/capteurs")
public class CapteurController {

    private final SerreService serreService;

    public CapteurController(SerreService serreService) {
        this.serreService = serreService;
    }

    @PostMapping
    public ResponseEntity<CapteurResponse> installer(@RequestBody CreerCapteurRequest requete) {
        CapteurResponse reponse = serreService.ajouterCapteur(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/serre/{serreId}")
    public ResponseEntity<List<CapteurResponse>> parSerre(@PathVariable Long serreId) {
        return ResponseEntity.ok(serreService.listerCapteursSerre(serreId));
    }
}
