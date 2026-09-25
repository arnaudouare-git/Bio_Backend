package com.example.bio_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Gere la plateforme (validation des inscriptions, suivi global).
 * Herite de tous les champs communs de Utilisateur, aucun champ propre.
 */
@Entity
@Table(name = "administrateurs")
public class Administrateur extends Utilisateur {
    // Pas d'attribut supplementaire pour le moment.
    // Les actions ajouter()/modifier()/creer() seront implementees
    // plus tard dans un service (AdministrateurService), pas ici.
}
