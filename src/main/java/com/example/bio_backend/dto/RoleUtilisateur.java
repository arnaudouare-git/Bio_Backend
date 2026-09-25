package com.example.bio_backend.dto;

/**
 * Role choisi par l'utilisateur au moment de l'inscription
 * (RegisterRequest.role) :
 *
 *   - PRODUCTEUR : produit, vend ET achete des larves aupres d'autres
 *     producteurs. Formation (hors plateforme) + verification de la piece
 *     d'identite (CNIB) par un Administrateur obligatoires avant activation
 *     du compte -- voir AdminService.
 *
 *   - CLIENT : achete des larves SEULEMENT (jamais de vente ni de
 *     production). Inscription libre et immediate : ni formation, ni CNIB.
 *
 * Voir AuthService.inscrire(), qui aiguille vers inscrireProducteur() ou
 * inscrireClient() selon cette valeur.
 */
public enum RoleUtilisateur {
    PRODUCTEUR,
    CLIENT
}
