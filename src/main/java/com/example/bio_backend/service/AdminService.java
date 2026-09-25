package com.example.bio_backend.service;

import com.example.bio_backend.dto.CreerFormationRequest;
import com.example.bio_backend.dto.FormationResponse;
import com.example.bio_backend.dto.StatistiquesResponse;
import com.example.bio_backend.entity.Avis;
import com.example.bio_backend.entity.Formation;
import com.example.bio_backend.entity.Paiement;
import com.example.bio_backend.entity.Notification;
import com.example.bio_backend.entity.Producteur;
import com.example.bio_backend.entity.Utilisateur;
import com.example.bio_backend.exception.CompteNonEligibleActivationException;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.repository.AvisRepository;
import com.example.bio_backend.repository.ClientRepository;
import com.example.bio_backend.repository.CommandeRepository;
import com.example.bio_backend.repository.FormationRepository;
import com.example.bio_backend.repository.LitigeRepository;
import com.example.bio_backend.repository.NotificationRepository;
import com.example.bio_backend.repository.PaiementRepository;
import com.example.bio_backend.repository.ProducteurRepository;
import com.example.bio_backend.repository.ProduitRepository;
import com.example.bio_backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final CommandeRepository commandeRepository;
    private final PaiementRepository paiementRepository;
    private final ProduitRepository produitRepository;
    private final AvisRepository avisRepository;
    private final LitigeRepository litigeRepository;

    public AdminService(FormationRepository formationRepository,
                         ProducteurRepository producteurRepository,
                         NotificationRepository notificationRepository,
                         UtilisateurRepository utilisateurRepository,
                         ClientRepository clientRepository,
                         CommandeRepository commandeRepository,
                         PaiementRepository paiementRepository,
                         ProduitRepository produitRepository,
                         AvisRepository avisRepository,
                         LitigeRepository litigeRepository) {
        this.formationRepository = formationRepository;
        this.producteurRepository = producteurRepository;
        this.notificationRepository = notificationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.clientRepository = clientRepository;
        this.commandeRepository = commandeRepository;
        this.paiementRepository = paiementRepository;
        this.produitRepository = produitRepository;
        this.avisRepository = avisRepository;
        this.litigeRepository = litigeRepository;
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

    /**
     * Module Suspension de compte (ajoute le 2026-09-24, priorite Tier 1) :
     * suspend n'importe quel compte (Producteur OU Client, jamais un autre
     * Administrateur au niveau service -- a restreindre cote appelant si
     * besoin) sans le supprimer. Le compte ne peut plus se connecter tant
     * qu'il n'a pas ete reactive (voir AuthService.connecter et
     * reactiverCompte() ci-dessous).
     */
    @Transactional
    public void suspendreCompte(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Utilisateur introuvable : id=" + utilisateurId));
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    /** Leve la suspension posee par suspendreCompte() ci-dessus. */
    @Transactional
    public void reactiverCompte(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Utilisateur introuvable : id=" + utilisateurId));
        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);
    }

    /**
     * Statistiques globales de la plateforme (module Tier 2, ajoute le
     * 2026-09-24) -- destinees au tableau de bord Admin. Le chiffre
     * d'affaires est calcule a partir des Paiements au statut REUSSI (et
     * non du montantTotal des commandes), pour ne compter que l'argent
     * effectivement encaisse.
     */
    @Transactional(readOnly = true)
    public StatistiquesResponse obtenirStatistiques() {
        long nombreProducteurs = producteurRepository.count();
        long nombreProducteursVerifies = producteurRepository.findAll().stream()
                .filter(p -> "VERIFIE".equals(p.getStatutVerificationCnib()))
                .count();
        long nombreClients = clientRepository.count();
        long nombreCommandes = commandeRepository.count();
        long nombreCommandesLivrees = commandeRepository.findByStatut("LIVREE").size();

        List<Paiement> paiementsReussis = paiementRepository.findAll().stream()
                .filter(p -> "REUSSI".equals(p.getStatut()))
                .toList();

        BigDecimal chiffreAffairesTotal = paiementsReussis.stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Commission plateforme (ajoutee le 2026-09-24, retour client) : somme des
        // commissions deja calculees a la confirmation de chaque paiement REUSSI
        // (voir PaiementService.confirmerPaiement()).
        BigDecimal commissionTotale = paiementsReussis.stream()
                .map(Paiement::getMontantCommission)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nombreProduitsPublies = produitRepository.count();
        long nombreFormations = formationRepository.count();
        long nombreParticipantsFormes = formationRepository.findAll().stream()
                .mapToLong(f -> f.getParticipants().size())
                .sum();

        List<Avis> tousLesAvis = avisRepository.findAll();
        long nombreAvis = tousLesAvis.size();
        Double noteMoyenne = tousLesAvis.isEmpty() ? null
                : tousLesAvis.stream().mapToInt(Avis::getNote).average().orElse(0.0);

        long nombreLitigesOuverts = litigeRepository.findAll().stream()
                .filter(l -> "OUVERT".equals(l.getStatut()))
                .count();

        return new StatistiquesResponse(nombreProducteurs, nombreProducteursVerifies, nombreClients,
                nombreCommandes, nombreCommandesLivrees, chiffreAffairesTotal, commissionTotale,
                nombreProduitsPublies, nombreFormations, nombreParticipantsFormes, nombreAvis,
                noteMoyenne, nombreLitigesOuverts);
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
