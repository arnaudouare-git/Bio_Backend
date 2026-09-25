package com.example.bio_backend.service;

import com.example.bio_backend.dto.AlerteResponse;
import com.example.bio_backend.dto.CreerMesureRequest;
import com.example.bio_backend.dto.MesureResponse;
import com.example.bio_backend.entity.Alerte;
import com.example.bio_backend.entity.Capteur;
import com.example.bio_backend.entity.Mesure;
import com.example.bio_backend.entity.Serre;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.repository.AlerteRepository;
import com.example.bio_backend.repository.CapteurRepository;
import com.example.bio_backend.repository.MesureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logique metier de la partie "donnees temps reel" du module IoT :
 * enregistrer une mesure envoyee par un capteur, declencher automatiquement
 * une Alerte si un seuil du CDC est depasse, et gerer le cycle de vie des
 * alertes (liste, traitement).
 *
 * Seuils exacts repris du CDC (tableau "Systeme d'alertes automatiques") :
 *   temperature < 25°C  -> TEMPERATURE_BASSE
 *   temperature > 39°C  -> TEMPERATURE_ELEVEE
 *   humidite    < 50%   -> HUMIDITE_BASSE
 *   humidite    > 80%   -> HUMIDITE_ELEVEE
 * (le 5e cas du CDC, "capteur silencieux > 5 min", est calcule a la volee
 * dans SerreService.obtenirDashboard() plutot que declenche ici -- voir la
 * javadoc de cette methode.)
 */
@Service
public class MesureService {

    private static final double SEUIL_TEMPERATURE_BASSE = 25.0;
    private static final double SEUIL_TEMPERATURE_ELEVEE = 39.0;
    private static final double SEUIL_HUMIDITE_BASSE = 50.0;
    private static final double SEUIL_HUMIDITE_ELEVEE = 80.0;

    private final MesureRepository mesureRepository;
    private final CapteurRepository capteurRepository;
    private final AlerteRepository alerteRepository;
    private final ActionneurService actionneurService;

    public MesureService(MesureRepository mesureRepository, CapteurRepository capteurRepository,
                          AlerteRepository alerteRepository, ActionneurService actionneurService) {
        this.mesureRepository = mesureRepository;
        this.capteurRepository = capteurRepository;
        this.alerteRepository = alerteRepository;
        this.actionneurService = actionneurService;
    }

    @Transactional
    public MesureResponse enregistrerMesure(CreerMesureRequest requete) {
        Capteur capteur = capteurRepository.findById(requete.getCapteurId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Capteur introuvable : id=" + requete.getCapteurId()));

        Mesure mesure = new Mesure();
        mesure.setCapteur(capteur);
        mesure.setTemperature(requete.getTemperature());
        mesure.setHumidite(requete.getHumidite());

        Mesure enregistree = mesureRepository.save(mesure);

        // Verification des seuils du CDC -- une seule mesure peut declencher
        // a la fois une alerte temperature ET une alerte humidite.
        Serre serre = capteur.getSerre();
        Double temperature = requete.getTemperature();
        Double humidite = requete.getHumidite();

        if (temperature != null) {
            if (temperature < SEUIL_TEMPERATURE_BASSE) {
                declencherAlerteSiNecessaire(serre, "TEMPERATURE_BASSE", SEUIL_TEMPERATURE_BASSE);
            } else if (temperature > SEUIL_TEMPERATURE_ELEVEE) {
                declencherAlerteSiNecessaire(serre, "TEMPERATURE_ELEVEE", SEUIL_TEMPERATURE_ELEVEE);
            }
        }
        if (humidite != null) {
            if (humidite < SEUIL_HUMIDITE_BASSE) {
                declencherAlerteSiNecessaire(serre, "HUMIDITE_BASSE", SEUIL_HUMIDITE_BASSE);
            } else if (humidite > SEUIL_HUMIDITE_ELEVEE) {
                declencherAlerteSiNecessaire(serre, "HUMIDITE_ELEVEE", SEUIL_HUMIDITE_ELEVEE);
            }

            // Pilotage automatique de l'irrigation (ajoute le 2026-09-24, retour
            // client) : reutilise le meme seuil que l'alerte HUMIDITE_BASSE,
            // aucune nouvelle valeur inventee -- voir ActionneurService.
            actionneurService.piloterIrrigationAutomatiquement(serre, humidite, SEUIL_HUMIDITE_BASSE);
        }

        return versReponseMesure(enregistree);
    }

    /**
     * Cree une Alerte NON_TRAITEE pour ce type/cette serre, sauf s'il en
     * existe deja une NON_TRAITEE du meme type -- evite de spammer une
     * nouvelle alerte a chaque mesure tant que le probleme n'est pas resolu
     * (ou pas encore marque comme traite).
     */
    private void declencherAlerteSiNecessaire(Serre serre, String type, double seuil) {
        boolean dejaEnCours = alerteRepository.findBySerreIdAndStatut(serre.getId(), "NON_TRAITEE").stream()
                .anyMatch(a -> type.equals(a.getType()));

        if (!dejaEnCours) {
            Alerte alerte = new Alerte();
            alerte.setSerre(serre);
            alerte.setType(type);
            alerte.setSeuil(seuil);
            alerteRepository.save(alerte);
        }
    }

    public List<MesureResponse> listerHistoriqueCapteur(Long capteurId) {
        return mesureRepository.findTop50ByCapteurIdOrderByDateMesureDesc(capteurId).stream()
                .map(this::versReponseMesure)
                .toList();
    }

    public List<AlerteResponse> listerAlertesSerre(Long serreId) {
        return alerteRepository.findBySerreIdAndStatut(serreId, "NON_TRAITEE").stream()
                .map(a -> new AlerteResponse(a.getId(), a.getType(), a.getSeuil(),
                        a.getDateAlerte(), a.getStatut(), serreId))
                .toList();
    }

    @Transactional
    public AlerteResponse traiterAlerte(Long alerteId) {
        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new RessourceIntrouvableException("Alerte introuvable : id=" + alerteId));

        alerte.setStatut("TRAITEE");
        Alerte enregistree = alerteRepository.save(alerte);

        return new AlerteResponse(enregistree.getId(), enregistree.getType(), enregistree.getSeuil(),
                enregistree.getDateAlerte(), enregistree.getStatut(), enregistree.getSerre().getId());
    }

    private MesureResponse versReponseMesure(Mesure mesure) {
        return new MesureResponse(
                mesure.getId(),
                mesure.getTemperature(),
                mesure.getHumidite(),
                mesure.getDateMesure(),
                mesure.getCapteur().getId()
        );
    }
}
