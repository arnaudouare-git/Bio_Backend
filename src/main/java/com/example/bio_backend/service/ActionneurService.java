package com.example.bio_backend.service;

import com.example.bio_backend.dto.ActionneurResponse;
import com.example.bio_backend.entity.Actionneur;
import com.example.bio_backend.entity.Serre;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.repository.ActionneurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Pilotage automatique des actionneurs (ajoute le 2026-09-24, retour client
 * en entretien : "pilotage automatique pour arroser la serre"). Simule un
 * actionneur physique -- pas de vrai materiel pilote, juste un etat
 * ACTIF/INACTIF trace en base -- change automatiquement par MesureService
 * en fonction des mesures d'humidite, sur le meme principe que les Alertes
 * (voir MesureService.declencherAlerteSiNecessaire()) : un seuil du CDC
 * deja utilise pour l'alerte HUMIDITE_BASSE (< 50%) declenche ici aussi
 * l'irrigation, sans dupliquer une nouvelle valeur inventee.
 */
@Service
public class ActionneurService {

    private static final String TYPE_IRRIGATION = "IRRIGATION";
    private static final Set<String> ETATS_VALIDES = Set.of("ACTIF", "INACTIF");

    private final ActionneurRepository actionneurRepository;

    public ActionneurService(ActionneurRepository actionneurRepository) {
        this.actionneurRepository = actionneurRepository;
    }

    /**
     * Appelee par MesureService a chaque mesure d'humidite recue. Cree
     * l'actionneur IRRIGATION de la serre s'il n'existe pas encore (une
     * serre en a au plus un). Ne touche a rien (pas de nouvelle date) si
     * l'etat calcule est deja l'etat courant, pour ne pas ecraser un
     * pilotage manuel recent sans raison.
     */
    @Transactional
    public void piloterIrrigationAutomatiquement(Serre serre, double humidite, double seuilHumiditeBasse) {
        Actionneur actionneur = actionneurRepository.findBySerreIdAndType(serre.getId(), TYPE_IRRIGATION)
                .orElseGet(() -> {
                    Actionneur nouveau = new Actionneur();
                    nouveau.setSerre(serre);
                    nouveau.setType(TYPE_IRRIGATION);
                    return nouveau;
                });

        String nouvelEtat = humidite < seuilHumiditeBasse ? "ACTIF" : "INACTIF";
        if (!nouvelEtat.equals(actionneur.getEtat())) {
            actionneur.setEtat(nouvelEtat);
            actionneur.setDateDernierChangement(LocalDateTime.now());
        }
        actionneurRepository.save(actionneur);
    }

    /** Lecture publique, comme le reste du monitoring IoT (dashboard, mesures, alertes). */
    public List<ActionneurResponse> listerActionneursSerre(Long serreId) {
        return actionneurRepository.findBySerreId(serreId).stream()
                .map(this::versReponse)
                .toList();
    }

    /**
     * Pilotage manuel (reserve Producteur/Admin, voir SecurityConfig -- meme
     * regle que les autres ecritures du module IoT). Peut etre ecrase par la
     * prochaine mesure automatique si le seuil d'humidite est toujours
     * depasse -- volontairement pas de mode "auto/manuel" separe pour rester
     * simple, coherent avec le reste du module IoT actuel.
     */
    @Transactional
    public ActionneurResponse changerEtatManuel(Long actionneurId, String nouvelEtat) {
        if (nouvelEtat == null || !ETATS_VALIDES.contains(nouvelEtat)) {
            throw new IllegalArgumentException(
                    "Etat invalide : \"" + nouvelEtat + "\". Valeurs acceptees : " + ETATS_VALIDES);
        }
        Actionneur actionneur = actionneurRepository.findById(actionneurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Actionneur introuvable : id=" + actionneurId));

        actionneur.setEtat(nouvelEtat);
        actionneur.setDateDernierChangement(LocalDateTime.now());
        return versReponse(actionneurRepository.save(actionneur));
    }

    private ActionneurResponse versReponse(Actionneur actionneur) {
        return new ActionneurResponse(
                actionneur.getId(),
                actionneur.getType(),
                actionneur.getEtat(),
                actionneur.getDateDernierChangement(),
                actionneur.getSerre().getId()
        );
    }
}
