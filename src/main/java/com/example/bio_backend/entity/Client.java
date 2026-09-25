package com.example.bio_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Represente un Client : achete des larves SEULEMENT -- il ne produit
 * jamais, ne vend jamais.
 *
 * Contrairement au Producteur, son inscription est LIBRE et IMMEDIATE : ni
 * formation (hors plateforme), ni verification de piece d'identite (CNIB)
 * requises (voir AuthService.inscrireClient, appele quand
 * RegisterRequest.role == RoleUtilisateur.CLIENT). Son
 * statutVerificationCnib est mis directement a "VERIFIE" a la creation.
 *
 * Herite de tous les champs communs de Utilisateur, aucun champ propre : pas
 * de latitude/longitude (pas de localisation marchande a publier), pas de
 * formation (non applicable).
 */
@Entity
@Table(name = "clients")
public class Client extends Utilisateur {
    // Pas d'attribut supplementaire pour le moment.
}
