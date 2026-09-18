package com.example.bio_backend.service;

import com.example.bio_backend.dto.ConfirmerPaiementRequest;
import com.example.bio_backend.dto.CreerPaiementRequest;
import com.example.bio_backend.dto.PaiementResponse;
import com.example.bio_backend.entity.Commande;
import com.example.bio_backend.entity.Paiement;
import com.example.bio_backend.exception.PaiementDejaTraiteException;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.repository.CommandeRepository;
import com.example.bio_backend.repository.PaiementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Logique metier du paiement (simulation Orange Money + especes).
 *
 * Regle centrale (meme pattern anti-triche que dans CommandeService pour le
 * prix des produits) : le montant d'un paiement n'est JAMAIS fourni par le
 * client. Il est toujours relu depuis Commande.montantTotal, cote serveur.
 *
 * Comme on n'a pas de vrai compte marchand Orange Money, la confirmation
 * (reussi/echoue) est SIMULEE via l'endpoint PUT /api/paiements/{id}/confirmer
 * plutot que recue d'un vrai webhook operateur.
 */
@Service
public class PaiementService {

    private static final Set<String> METHODES_VALIDES = Set.of("ORANGE_MONEY", "CASH");

    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;

    public PaiementService(PaiementRepository paiementRepository, CommandeRepository commandeRepository) {
        this.paiementRepository = paiementRepository;
        this.commandeRepository = commandeRepository;
    }

    @Transactional
    public PaiementResponse initierPaiement(CreerPaiementRequest requete) {
        Commande commande = commandeRepository.findById(requete.getCommandeId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Commande introuvable : id=" + requete.getCommandeId()));

        String methode = requete.getMethode();
        if (methode == null || !METHODES_VALIDES.contains(methode)) {
            throw new IllegalArgumentException(
                    "Methode de paiement invalide : \"" + methode + "\". Valeurs acceptees : " + METHODES_VALIDES);
        }

        boolean dejaPayee = paiementRepository.findByCommandeId(commande.getId()).stream()
                .anyMatch(p -> "REUSSI".equals(p.getStatut()));
        if (dejaPayee) {
            throw new PaiementDejaTraiteException(
                    "Cette commande a deja un paiement reussi : id=" + commande.getId());
        }

        Paiement paiement = new Paiement();
        paiement.setCommande(commande);
        paiement.setMethode(methode);
        // Montant jamais fourni par le client : toujours relu depuis la commande.
        paiement.setMontant(commande.getMontantTotal());

        if ("ORANGE_MONEY".equals(methode)) {
            // Simule la reference que renverrait l'operateur a l'initiation
            // de la transaction (avant confirmation du succes/echec).
            paiement.setReferenceTransaction("OM-" + UUID.randomUUID().toString()
                    .substring(0, 8).toUpperCase());
            // statut reste EN_ATTENTE (fixe par @PrePersist) : on attend la confirmation.
        }
        // Pour CASH : pas de reference de transaction (paiement constate a la
        // livraison), et pas de confirmation via l'API -- voir confirmerPaiement().

        Paiement enregistre = paiementRepository.save(paiement);
        return versReponse(enregistre);
    }

    @Transactional
    public PaiementResponse confirmerPaiement(Long paiementId, ConfirmerPaiementRequest requete) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Paiement introuvable : id=" + paiementId));

        if ("CASH".equals(paiement.getMethode())) {
            throw new IllegalArgumentException(
                    "Un paiement en especes ne se confirme pas via cet endpoint "
                            + "(il n'y a pas de transaction Orange Money a confirmer) ; "
                            + "il est constate manuellement a la livraison.");
        }

        if (!"EN_ATTENTE".equals(paiement.getStatut())) {
            throw new PaiementDejaTraiteException(
                    "Ce paiement a deja ete traite (statut actuel : " + paiement.getStatut() + ").");
        }

        if (requete.isReussi()) {
            paiement.setStatut("REUSSI");
            // La commande passe automatiquement a CONFIRMEE une fois payee.
            Commande commande = paiement.getCommande();
            commande.setStatut("CONFIRMEE");
            commandeRepository.save(commande);
        } else {
            paiement.setStatut("ECHOUE");
        }

        Paiement enregistre = paiementRepository.save(paiement);
        return versReponse(enregistre);
    }

    public List<PaiementResponse> listerPaiementsCommande(Long commandeId) {
        if (!commandeRepository.existsById(commandeId)) {
            throw new RessourceIntrouvableException("Commande introuvable : id=" + commandeId);
        }
        return paiementRepository.findByCommandeId(commandeId).stream()
                .map(this::versReponse)
                .toList();
    }

    private PaiementResponse versReponse(Paiement paiement) {
        return new PaiementResponse(
                paiement.getId(),
                paiement.getCommande().getId(),
                paiement.getMontant(),
                paiement.getMethode(),
                paiement.getStatut(),
                paiement.getReferenceTransaction(),
                paiement.getDatePaiement()
        );
    }
}
