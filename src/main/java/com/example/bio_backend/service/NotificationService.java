package com.example.bio_backend.service;

import com.example.bio_backend.dto.NotificationResponse;
import com.example.bio_backend.entity.Administrateur;
import com.example.bio_backend.entity.Commande;
import com.example.bio_backend.entity.LigneCommande;
import com.example.bio_backend.entity.Notification;
import com.example.bio_backend.entity.Producteur;
import com.example.bio_backend.entity.Utilisateur;
import com.example.bio_backend.exception.CommandeAccesRefuseException;
import com.example.bio_backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Notifications internes a l'application (module ajoute le 2026-09-24 pour
 * combler la dette notee dans Module_Commandes_2026-09-23.md : un Producteur
 * n'etait jamais informe a la reception d'une nouvelle commande sur l'un de
 * ses produits). Reutilise l'entite Notification deja modelisee au tout
 * debut du projet (canal SMS/EMAIL prevu a l'origine) avec un nouveau canal
 * "APP" : pas d'envoi SMS/email reel, juste un enregistrement que le
 * Producteur peut consulter via GET /api/notifications/utilisateur/{id}.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Appelee par CommandeService juste apres l'enregistrement d'une nouvelle
     * commande : une Notification par Producteur concerne (une commande peut
     * porter sur les produits de plusieurs Producteurs differents), avec le
     * detail de SES lignes uniquement -- jamais celles des autres.
     */
    @Transactional
    public void notifierReceptionCommande(Commande commande) {
        Map<Long, List<LigneCommande>> lignesParProducteurId = new LinkedHashMap<>();
        for (LigneCommande ligne : commande.getLignes()) {
            Long producteurId = ligne.getProduit().getProducteur().getId();
            lignesParProducteurId.computeIfAbsent(producteurId, id -> new ArrayList<>()).add(ligne);
        }

        for (List<LigneCommande> lignes : lignesParProducteurId.values()) {
            Producteur producteur = lignes.get(0).getProduit().getProducteur();

            BigDecimal sousTotal = lignes.stream()
                    .map(l -> l.getPrixUnitaire().multiply(BigDecimal.valueOf(l.getQuantite())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String message = "Nouvelle commande #" + commande.getId() + " recue : "
                    + lignes.size() + " ligne(s) sur vos produits, pour un total de "
                    + sousTotal + " FCFA.";

            Notification notification = new Notification();
            notification.setUtilisateur(producteur);
            notification.setCanal("APP");
            notification.setMessage(message);
            notification.setStatutEnvoi("ENVOYE");
            notificationRepository.save(notification);
        }
    }

    /** Meme regle de propriete que listerLitigesUtilisateur() dans LitigeService. */
    public List<NotificationResponse> listerNotificationsUtilisateur(Long utilisateurId, Utilisateur demandeur) {
        if (!(demandeur instanceof Administrateur) && !utilisateurId.equals(demandeur.getId())) {
            throw new CommandeAccesRefuseException("Vous ne pouvez consulter que vos propres notifications.");
        }
        return notificationRepository.findByUtilisateurIdOrderByDateEnvoiDesc(utilisateurId).stream()
                .map(this::versReponse)
                .toList();
    }

    private NotificationResponse versReponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getCanal(),
                notification.getMessage(),
                notification.getStatutEnvoi(),
                notification.getDateEnvoi()
        );
    }
}
