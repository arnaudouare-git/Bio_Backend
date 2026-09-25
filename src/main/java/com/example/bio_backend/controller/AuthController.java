package com.example.bio_backend.controller;

import com.example.bio_backend.dto.AuthResponse;
import com.example.bio_backend.dto.LoginRequest;
import com.example.bio_backend.dto.RegisterRequest;
import com.example.bio_backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST du module Authentification.
 *
 * v3 du modele (2026-09-11) : DEUX roles possibles au champ "role" de
 * RegisterRequest -- PRODUCTEUR ou CLIENT (voir RoleUtilisateur).
 *
 * Testable tout de suite avec Postman, avant meme qu'Angular existe :
 *
 * POST http://localhost:8080/api/auth/inscription  (Producteur)
 * {
 *   "nom": "Ouedraogo",
 *   "prenom": "Awa",
 *   "email": "awa@example.com",
 *   "motDePasse": "motdepasse123",
 *   "telephone": "70000000",
 *   "role": "PRODUCTEUR",
 *   "documentIdentiteRef": "B00123456",
 *   "latitude": 12.3714,
 *   "longitude": -1.5197
 * }
 * -> 201 CREATED, compte cree avec statutVerificationCnib = "EN_ATTENTE".
 *    Il ne devient actif qu'apres la formation + verification CNIB par un
 *    Administrateur (voir AdminController).
 *
 * POST http://localhost:8080/api/auth/inscription  (Client)
 * {
 *   "nom": "Dupont",
 *   "prenom": "Alice",
 *   "email": "alice@example.com",
 *   "motDePasse": "motdepasse123",
 *   "telephone": "70000001",
 *   "role": "CLIENT"
 * }
 * -> 201 CREATED, compte deja actif (statutVerificationCnib = "VERIFIE"),
 *    documentIdentiteRef n'est pas requis pour ce role.
 *
 * POST http://localhost:8080/api/auth/connexion
 * {
 *   "email": "awa@example.com",
 *   "motDePasse": "motdepasse123"
 * }
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/inscription")
    public ResponseEntity<AuthResponse> inscrire(@RequestBody RegisterRequest requete) {
        AuthResponse reponse = authService.inscrire(requete);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @PostMapping("/connexion")
    public ResponseEntity<AuthResponse> connecter(@RequestBody LoginRequest requete) {
        AuthResponse reponse = authService.connecter(requete);
        return ResponseEntity.ok(reponse);
    }
}
