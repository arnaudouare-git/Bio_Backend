package com.example.bio_backend.controller;

import com.example.bio_backend.dto.AlerteResponse;
import com.example.bio_backend.service.MesureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST des alertes (declenchees automatiquement par MesureService
 * quand un seuil du CDC est depasse).
 *
 * Testable avec Postman :
 *
 * GET http://localhost:8080/api/alertes/serre/1
 * -> alertes NON_TRAITEE de cette serre.
 *
 * PUT http://localhost:8080/api/alertes/1/traiter
 * -> marque l'alerte comme TRAITEE (le Producteur/Admin confirme avoir agi).
 * -> 404 NOT FOUND si alerteId n'existe pas.
 */
@RestController
@RequestMapping("/api/alertes")
public class AlerteController {

    private final MesureService mesureService;

    public AlerteController(MesureService mesureService) {
        this.mesureService = mesureService;
    }

    @GetMapping("/serre/{serreId}")
    public ResponseEntity<List<AlerteResponse>> parSerre(@PathVariable Long serreId) {
        return ResponseEntity.ok(mesureService.listerAlertesSerre(serreId));
    }

    @PutMapping("/{alerteId}/traiter")
    public ResponseEntity<AlerteResponse> traiter(@PathVariable Long alerteId) {
        return ResponseEntity.ok(mesureService.traiterAlerte(alerteId));
    }
}
