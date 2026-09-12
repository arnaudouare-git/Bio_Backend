package com.example.bio_backend.service;

import com.example.bio_backend.dto.CreerFormationRequest;
import com.example.bio_backend.dto.FormationResponse;
import com.example.bio_backend.entity.Formation;
import com.example.bio_backend.entity.Notification;
import com.example.bio_backend.entity.Producteur;
import com.example.bio_backend.exception.CompteNonEligibleActivationException;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.repository.FormationRepository;
import com.example.bio_backend.repository.NotificationRepository;
import com.example.bio_backend.repository.ProducteurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logique metier reservee a l'Administrateur pour la formation obligatoire
 * et l'activation des comptes Producteur.
 *
 * Rappel de la regle metier (validee avec l'equipe le 2026-09-09) : la
 * formation reste une etape HORS plateforme (en presentiel), mais elle DOIT
 * etre tracee ici comme une vraie entite -- Formation -- pour savoir combien
 * de personnes ont ete formees. Un compte Producteur reste EN_ATTENTE et ne
 * peut ni acheter ni vendre tant qu'un Administrateur n'a pas :
 *   1. enregistre sa participation a une session de Formation, PUIS
 *   2. verifie sa piece d'identite (CNIB) et active le compte.
 */
@Service
public class AdminService {

    private final FormationRepository formationRepository;
    private final ProducteurRepository producteurRepository;
    private final NotificationRepository notificationRepository;

    public AdminService(FormationRepository formationRepository,
                         ProducteurRepository producteurRepository,
                         NotificationRepository notificationRepository) {
        this.formationRepository = formationRepository;
        this.producteurRepository = producteurRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public FormationResponse creerFormation(CreerFormationRequest requete) {
        Formation formation = new Formation();
        formation.setTitre(requete.getTitre());
        formation.setDateSession(requete.getDateSession());
        formation.setLieu(requete.getLieu());
        formation.setFormateur(requete.getFormateur());

        Formation enregistree = formationRepository.save(formation);
        return versReponse(enregistree);
    }

    /**
     * Liste des formations avec, pour chacune, son nombre de participants --
     * c'est le chiffre de traçabilite ("combien de personnes ont suivi la
     * formation") demande par l'utilisateur, utilise par exemple pour la
     * carte "Producteurs formes" du tableau de bord admin.
     */
    public List<FormationResponse> listerFormations() {
        return formationRepository.findAll().stream()
                .map(this::versReponse)
                .toList();
    }

    /**
     * Enregistre qu'un Producteur a suivi une session de Formation (donnee
     * hors plateforme). Ne change PAS encore le statut du compte : il faut
     * ensuite verifier sa piece d'identite via activerCompte().
     */
    @Transactional
    public void enregistrerParticipant(Long formationId, Long producteurId) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Formation introuvable : id=" + formationId));

        Producteur producteur = producteurRepository.findById(producteurId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Producteur introuvable : id=" + producteurId));

        producteur.setFormation(formation);
        producteurRepository.save(producteur);
    }

    /**
     * Active le compte d'un Producteur, APRES verification manuelle de sa
     * piece d'identite par un Administrateur. Refuse si le producteur n'a
     * pas encore suivi de formation -- c'est la regle metier centrale de
     * cette v2 du modele.
     */
    @Transactional
    public void activerCompte(Long producteurId) {
        Producteur producteur = producteurRepository.findById(producteurId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Producteur introuvable : id=" + producteurId));

        if (producteur.getFormation() == null) {
            throw new CompteNonEligibleActivationException(
                    "Impossible d'activer ce compte : le producteur n'a pas encore suivi de formation.");
        }

        producteur.setStatutVerificationCnib("VERIFIE");
        producteurRepository.save(producteur);

        Notification notification = new Notification();
        notification.setUtilisateur(producteur);
        notification.setDestinataire(producteur.getEmail());
        notification.setCanal("EMAIL");
        notification.setMessage(
                "Votre compte BioConversion est active. Vous pouvez maintenant acheter et vendre "
                        + "des larves sur la plateforme.");
        notificationRepository.save(notification);
    }

    private FormationResponse versReponse(Formation formation) {
        return new FormationResponse(
                formation.getId(),
                formation.getTitre(),
                formation.getDateSession(),
                formation.getLieu(),
                formation.getFormateur(),
                formation.getParticipants().size()
        );
    }
}
