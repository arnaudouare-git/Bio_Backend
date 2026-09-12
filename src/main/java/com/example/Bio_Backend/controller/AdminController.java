package com.example.bio_backend.controller;

import com.example.bio_backend.dto.CreerFormationRequest;
import com.example.bio_backend.dto.FormationResponse;
import com.example.bio_backend.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST reserves a l'Administrateur : gestion des formations
 * (traçabilite) et activation des comptes Producteur.
 *
 * NB : ces endpoints ne sont pas encore proteges par une authentification
 * (pas de Spring Security dans ce module) -- a securiser plus tard (ex :
 * restreindre a un role ADMINISTRATEUR une fois la couche securite ajoutee).
 *
 * Testable avec Postman :
 *
 * POST http://localhost:8080/api/admin/formations
 * {
 *   "titre": "Formation BioConversion - Session Ouaga",
 *   "dateSession": "2026-09-15T09:00:00",
 *   "lieu": "Ouagadougou",
 *   "formateur": "Miguel"
 * }
 *
 * GET http://localhost:8080/api/admin/formations
 * -> liste des formations, chacune avec nombreParticipants (traçabilite).
 *
 * POST http://localhost:8080/api/admin/formations/1/participants/5
 * -> enregistre que le Producteur id=5 a suivi la Formation id=1.
 *
 * PUT http://localhost:8080/api/admin/producteurs/5/activer
 * -> active le compte (apres verification manuelle de la piece d'identite).
 *    Echoue avec 400 si le producteur n'a pas encore suivi de formation.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/formations")
    public ResponseEntity<FormationResponse> creerFormation(@RequestBody CreerFormationRequest requete) {
        FormationResponse reponse = adminService.creerFormation(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/formations")
    public ResponseEntity<List<FormationResponse>> listerFormations() {
        return ResponseEntity.ok(adminService.listerFormations());
    }

    @PostMapping("/formations/{formationId}/participants/{producteurId}")
    public ResponseEntity<Void> enregistrerParticipant(@PathVariable Long formationId,
                                                        @PathVariable Long producteurId) {
        adminService.enregistrerParticipant(formationId, producteurId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/producteurs/{producteurId}/activer")
    public ResponseEntity<Void> activerCompte(@PathVariable Long producteurId) {
        adminService.activerCompte(producteurId);
        return ResponseEntity.noContent().build();
    }
}
