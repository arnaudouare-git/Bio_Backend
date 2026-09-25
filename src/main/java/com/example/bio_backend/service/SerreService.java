package com.example.bio_backend.service;

import com.example.bio_backend.dto.CapteurEtatResponse;
import com.example.bio_backend.dto.CapteurResponse;
import com.example.bio_backend.dto.CreerCapteurRequest;
import com.example.bio_backend.dto.CreerSerreRequest;
import com.example.bio_backend.dto.AlerteResponse;
import com.example.bio_backend.dto.SerreDashboardResponse;
import com.example.bio_backend.dto.SerreResponse;
import com.example.bio_backend.entity.Capteur;
import com.example.bio_backend.entity.Mesure;
import com.example.bio_backend.entity.Producteur;
import com.example.bio_backend.entity.Serre;
import com.example.bio_backend.exception.CompteNonVerifieException;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.repository.AlerteRepository;
import com.example.bio_backend.repository.CapteurRepository;
import com.example.bio_backend.repository.MesureRepository;
import com.example.bio_backend.repository.ProducteurRepository;
import com.example.bio_backend.repository.SerreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Logique metier de la partie "configuration" du module IoT : creer une
 * serre, y installer des capteurs, et fournir un dashboard temps reel.
 *
 * Meme regle anti-fraude que Produits/Commandes : seul un Producteur
 * VERIFIE peut creer une serre (geree a distance, donc reservee a un
 * compte deja controle par l'Administrateur).
 *
 * L'ingestion des mesures et la gestion des alertes sont dans
 * MesureService (partie "donnees temps reel" du module).
 */
@Service
public class SerreService {

    /** Seuil du CDC : au-dela, un capteur sans nouvelle mesure est considere "silencieux". */
    private static final long MINUTES_SILENCE_MAX = 5;

    private final SerreRepository serreRepository;
    private final CapteurRepository capteurRepository;
    private final MesureRepository mesureRepository;
    private final AlerteRepository alerteRepository;
    private final ProducteurRepository producteurRepository;

    public SerreService(SerreRepository serreRepository, CapteurRepository capteurRepository,
                         MesureRepository mesureRepository, AlerteRepository alerteRepository,
                         ProducteurRepository producteurRepository) {
        this.serreRepository = serreRepository;
        this.capteurRepository = capteurRepository;
        this.mesureRepository = mesureRepository;
        this.alerteRepository = alerteRepository;
        this.producteurRepository = producteurRepository;
    }

    @Transactional
    public SerreResponse creerSerre(CreerSerreRequest requete) {
        Producteur producteur = producteurRepository.findById(requete.getProducteurId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Producteur introuvable : id=" + requete.getProducteurId()));

        if (!"VERIFIE".equals(producteur.getStatutVerificationCnib())) {
            throw new CompteNonVerifieException(
                    "Impossible de creer une serre : ce compte n'est pas encore verifie "
                            + "(formation + verification de la piece d'identite requises).");
        }

        Serre serre = new Serre();
        serre.setNom(requete.getNom());
        serre.setLocalisation(requete.getLocalisation());
        serre.setCapaciteMax(requete.getCapaciteMax());
        serre.setProducteur(producteur);

        Serre enregistree = serreRepository.save(serre);
        return versReponseSerre(enregistree);
    }

    public List<SerreResponse> listerParProducteur(Long producteurId) {
        return serreRepository.findByProducteurId(producteurId).stream()
                .map(this::versReponseSerre)
                .toList();
    }

    public SerreResponse obtenirSerre(Long serreId) {
        Serre serre = serreRepository.findById(serreId)
                .orElseThrow(() -> new RessourceIntrouvableException("Serre introuvable : id=" + serreId));
        return versReponseSerre(serre);
    }

    @Transactional
    public CapteurResponse ajouterCapteur(CreerCapteurRequest requete) {
        Serre serre = serreRepository.findById(requete.getSerreId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Serre introuvable : id=" + requete.getSerreId()));

        Capteur capteur = new Capteur();
        capteur.setType(requete.getType());
        capteur.setDateInstallation(LocalDateTime.now());
        capteur.setSerre(serre);

        Capteur enregistre = capteurRepository.save(capteur);
        return versReponseCapteur(enregistre);
    }

    public List<CapteurResponse> listerCapteursSerre(Long serreId) {
        return capteurRepository.findBySerreId(serreId).stream()
                .map(this::versReponseCapteur)
                .toList();
    }

    /**
     * Vue temps reel d'une serre : derniere mesure de chaque capteur (avec
     * calcul "silencieux" a la volee) + alertes non traitees.
     */
    public SerreDashboardResponse obtenirDashboard(Long serreId) {
        Serre serre = serreRepository.findById(serreId)
                .orElseThrow(() -> new RessourceIntrouvableException("Serre introuvable : id=" + serreId));

        List<CapteurEtatResponse> etatsCapteurs = capteurRepository.findBySerreId(serreId).stream()
                .map(this::versEtatCapteur)
                .toList();

        List<AlerteResponse> alertes = alerteRepository.findBySerreIdAndStatut(serreId, "NON_TRAITEE").stream()
                .map(a -> new AlerteResponse(a.getId(), a.getType(), a.getSeuil(),
                        a.getDateAlerte(), a.getStatut(), serreId))
                .toList();

        return new SerreDashboardResponse(serre.getId(), serre.getNom(), etatsCapteurs, alertes);
    }

    private CapteurEtatResponse versEtatCapteur(Capteur capteur) {
        List<Mesure> dernieres = mesureRepository
                .findTop50ByCapteurIdOrderByDateMesureDesc(capteur.getId());

        if (dernieres.isEmpty()) {
            // Jamais recu de mesure du tout -> considere silencieux par defaut.
            return new CapteurEtatResponse(capteur.getId(), capteur.getType(), null, null, null, true);
        }

        Mesure derniere = dernieres.get(0);
        boolean silencieux = ChronoUnit.MINUTES.between(derniere.getDateMesure(), LocalDateTime.now())
                > MINUTES_SILENCE_MAX;

        return new CapteurEtatResponse(capteur.getId(), capteur.getType(),
                derniere.getTemperature(), derniere.getHumidite(), derniere.getDateMesure(), silencieux);
    }

    private SerreResponse versReponseSerre(Serre serre) {
        Producteur producteur = serre.getProducteur();
        int nombreCapteurs = capteurRepository.findBySerreId(serre.getId()).size();
        return new SerreResponse(
                serre.getId(),
                serre.getNom(),
                serre.getLocalisation(),
                serre.getCapaciteMax(),
                producteur.getId(),
                producteur.getNom() + " " + producteur.getPrenom(),
                nombreCapteurs
        );
    }

    private CapteurResponse versReponseCapteur(Capteur capteur) {
        return new CapteurResponse(
                capteur.getId(),
                capteur.getType(),
                capteur.getDateInstallation(),
                capteur.getSerre().getId()
        );
    }
}
