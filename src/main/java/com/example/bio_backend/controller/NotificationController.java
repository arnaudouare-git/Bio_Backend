package com.example.bio_backend.controller;

import com.example.bio_backend.config.UtilisateurDetailsImpl;
import com.example.bio_backend.dto.NotificationResponse;
import com.example.bio_backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST du module Notification (ajoute le 2026-09-24) : notifications
 * internes a l'application (pas d'envoi SMS/email reel), utilisees pour
 * l'instant pour informer un Producteur a la reception d'une nouvelle
 * commande sur l'un de ses produits (voir CommandeService.passerCommande()).
 *
 * Testable avec Postman :
 *
 * GET http://localhost:8080/api/notifications/utilisateur/3
 * Header : Authorization: Bearer <token>
 * -> toutes les notifications de cet utilisateur (les plus recentes en tete
 *    si NotificationRepository.findByUtilisateurId() est trie -- sinon ordre
 *    naturel). Reserve a lui-meme ou a un Administrateur.
 * -> 403 FORBIDDEN si l'appelant n'est ni cet utilisateur, ni un Administrateur.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<NotificationResponse>> listerParUtilisateur(
            @PathVariable Long utilisateurId,
            @AuthenticationPrincipal UtilisateurDetailsImpl principal) {
        return ResponseEntity.ok(
                notificationService.listerNotificationsUtilisateur(utilisateurId, principal.getUtilisateur()));
    }
}
