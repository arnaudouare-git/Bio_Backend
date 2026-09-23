package com.example.bio_backend.service;

import com.example.bio_backend.dto.ChangerStatutCommandeRequest;
import com.example.bio_backend.dto.CommandeResponse;
import com.example.bio_backend.dto.CreerCommandeRequest;
import com.example.bio_backend.dto.LigneCommandeRequest;
import com.example.bio_backend.dto.LigneCommandeResponse;
import com.example.bio_backend.entity.Administrateur;
import com.example.bio_backend.entity.Commande;
import com.example.bio_backend.entity.LigneCommande;
import com.example.bio_backend.entity.Produit;
import com.example.bio_backend.entity.Utilisateur;
import com.example.bio_backend.exception.CommandeAccesRefuseException;
import com.example.bio_backend.exception.CompteNonVerifieException;
import com.example.bio_backend.exception.RessourceIntrouvableException;
import com.example.bio_backend.exception.StockInsuffisantException;
import com.example.bio_backend.repository.CommandeRepository;
import com.example.bio_backend.repository.ProduitRepository;
import com.example.bio_backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Logique metier du passage de commande.
 *
 * Regle metier centrale de ce module (le controle qu'on reportait depuis le
 * module Authentification, cote ACHAT cette fois -- deja fait cote vente
 * dans ProduitService) : n'importe quel Utilisateur (Producteur OU Client)
 * peut acheter, MAIS seulement si son compte est VERIFIE. Comme l'acheteur
 * est type sur la classe abstraite Utilisateur (heritage JOINED), un seul
 * controle suffit pour les deux roles -- pas de branche par role ici.
 */
@Service
public class CommandeService {

    private static final Set<String> STATUTS_VALIDES = Set.of(
            "EN_ATTENTE", "CONFIRMEE", "EN_PREPARATION", "EXPEDIEE", "LIVREE", "ANNULEE");

    /**
     * Ordre de progression normal d'une ligne (hors ANNULEE, qui est un cas
     * a part -- voir changerStatutLigne()). Sert a empecher un Producteur
     * de faire reculer le statut ou de sauter des etapes n'importe comment.
     */
    private static final List<String> ORDRE_STATUTS = List.of(
            "EN_ATTENTE", "CONFIRMEE", "EN_PREPARATION", "EXPEDIEE", "LIVREE");

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;

    public CommandeService(CommandeRepository commandeRepository,
                            ProduitRepository produitRepository,
                            UtilisateurRepository utilisateurRepository) {
        this.commandeRepository = commandeRepository;
        this.produitRepository = produitRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * "demandeur" est l'utilisateur authentifie via le token JWT de la
     * requete (voir CommandeController) -- plus jamais un id envoye dans
     * le body, qui permettait avant de passer commande au nom de
     * n'importe qui (corrige le 2026-09-23). On le recharge quand meme
     * depuis le repository pour avoir une entite geree par Hibernate dans
     * CETTE transaction (l'objet issu du filtre JWT vient d'une requete
     * precedente, hors transaction).
     */
    @Transactional
    public CommandeResponse passerCommande(CreerCommandeRequest requete, Utilisateur demandeur) {
        Utilisateur acheteur = utilisateurRepository.findById(demandeur.getId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Utilisateur introuvable : id=" + demandeur.getId()));

        if (!"VERIFIE".equals(acheteur.getStatutVerificationCnib())) {
            throw new CompteNonVerifieException(
                    "Impossible de passer commande : ce compte n'est pas encore verifie "
                            + "(formation + verification de la piece d'identite requises pour un Producteur ; "
                            + "un Client est normalement verifie des l'inscription).");
        }

        if (requete.getLignes() == null || requete.getLignes().isEmpty()) {
            throw new IllegalArgumentException("Une commande doit contenir au moins une ligne (produit + quantite).");
        }

        Commande commande = new Commande();
        commande.setAcheteur(acheteur);
        commande.setAdresseLivraison(requete.getAdresseLivraison());

        for (LigneCommandeRequest ligneRequete : requete.getLignes()) {
            Produit produit = produitRepository.findById(ligneRequete.getProduitId())
                    .orElseThrow(() -> new RessourceIntrouvableException(
                            "Produit introuvable : id=" + ligneRequete.getProduitId()));

            if (produit.getStock() < ligneRequete.getQuantite()) {
                throw new StockInsuffisantException(
                        "Stock insuffisant pour \"" + produit.getNom() + "\" : demande="
                                + ligneRequete.getQuantite() + ", disponible=" + produit.getStock());
            }

            // Le prix est fige au moment de l'achat (copie depuis Produit),
            // pour que la commande reste correcte meme si le prix change
            // plus tard -- jamais fourni par le client dans la requete.
            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(ligneRequete.getQuantite());
            ligne.setPrixUnitaire(produit.getPrixUnitaire());
            ligne.setCommande(commande);
            commande.getLignes().add(ligne);

            produit.setStock(produit.getStock() - ligneRequete.getQuantite());
            produitRepository.save(produit);
        }

        commande.calculerMontantTotal();
        Commande enregistree = commandeRepository.save(commande);
        return versReponse(enregistree);
    }

    /**
     * "demandeur" doit etre soit l'acheteur lui-meme (acheteurId == son
     * propre id), soit un Administrateur -- sinon n'importe quel compte
     * connecte pouvait consulter les commandes de n'importe qui d'autre
     * juste en changeant l'id dans l'URL (corrige le 2026-09-23, meme
     * principe que la verification de propriete sur les produits).
     */
    public List<CommandeResponse> listerCommandesAcheteur(Long acheteurId, Utilisateur demandeur) {
        verifierProprietaireOuAdmin(acheteurId, demandeur);
        return commandeRepository.findByAcheteurId(acheteurId).stream()
                .map(this::versReponse)
                .toList();
    }

    /** Meme regle de propriete que listerCommandesAcheteur() ci-dessus -- voir sa javadoc. */
    public CommandeResponse obtenirCommande(Long commandeId, Utilisateur demandeur) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Commande introuvable : id=" + commandeId));
        verifierProprietaireOuAdmin(commande.getAcheteur().getId(), demandeur);
        return versReponse(commande);
    }

    private void verifierProprietaireOuAdmin(Long acheteurId, Utilisateur demandeur) {
        if (demandeur instanceof Administrateur) {
            return;
        }
        if (!acheteurId.equals(demandeur.getId())) {
            throw new CommandeAccesRefuseException("Vous ne pouvez consulter que vos propres commandes.");
        }
    }

    /**
     * PUT /api/commandes/{id}/statut -- reserve a l'ACHETEUR (annulation
     * uniquement, et seulement si aucun Producteur n'a encore commence a
     * traiter sa ligne) et a l'ADMINISTRATEUR (override complet, cascade
     * sur toutes les lignes -- vue d'ensemble). Un Producteur qui veut
     * faire avancer SA ligne doit utiliser changerStatutLigne() ci-dessous
     * (regle clarifiee le 2026-09-23 : avant, n'importe quel compte
     * connecte pouvait changer le statut de n'importe quelle commande).
     */
    @Transactional
    public CommandeResponse changerStatut(Long commandeId, ChangerStatutCommandeRequest requete, Utilisateur demandeur) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Commande introuvable : id=" + commandeId));

        String nouveauStatut = requete.getStatut();
        if (nouveauStatut == null || !STATUTS_VALIDES.contains(nouveauStatut)) {
            throw new IllegalArgumentException(
                    "Statut invalide : \"" + nouveauStatut + "\". Valeurs acceptees : " + STATUTS_VALIDES);
        }

        boolean estAdmin = demandeur instanceof Administrateur;
        boolean estAcheteur = commande.getAcheteur().getId().equals(demandeur.getId());

        if (estAdmin) {
            // Vue d'ensemble : l'Admin peut tout corriger, statut global et lignes alignes.
            commande.setStatut(nouveauStatut);
            commande.getLignes().forEach(ligne -> ligne.setStatut(nouveauStatut));
        } else if (estAcheteur) {
            if (!"ANNULEE".equals(nouveauStatut)) {
                throw new CommandeAccesRefuseException(
                        "En tant qu'acheteur, vous ne pouvez qu'annuler cette commande "
                                + "(le suivi par produit est gere par chaque producteur, "
                                + "voir PUT /api/commandes/{id}/lignes/{ligneId}/statut).");
            }
            boolean toutEncoreEnAttente = commande.getLignes().stream()
                    .allMatch(l -> "EN_ATTENTE".equals(l.getStatut()));
            if (!toutEncoreEnAttente) {
                throw new IllegalArgumentException(
                        "Cette commande ne peut plus etre annulee : au moins un producteur "
                                + "a deja commence a la traiter.");
            }
            commande.setStatut("ANNULEE");
            commande.getLignes().forEach(ligne -> ligne.setStatut("ANNULEE"));
        } else {
            throw new CommandeAccesRefuseException(
                    "Utilisez PUT /api/commandes/{id}/lignes/{ligneId}/statut pour changer le statut "
                            + "de vos propres produits dans cette commande.");
        }

        Commande enregistree = commandeRepository.save(commande);
        return versReponse(enregistree);
    }

    /**
     * PUT /api/commandes/{commandeId}/lignes/{ligneId}/statut -- chaque
     * Producteur fait avancer SA ligne (celle de son produit) sans toucher
     * aux lignes des autres Producteurs dans la meme commande, meme si la
     * commande est multi-producteurs. L'Administrateur peut forcer
     * n'importe quelle transition sur n'importe quelle ligne.
     */
    @Transactional
    public CommandeResponse changerStatutLigne(Long commandeId, Long ligneId,
                                                ChangerStatutCommandeRequest requete, Utilisateur demandeur) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Commande introuvable : id=" + commandeId));

        LigneCommande ligne = commande.getLignes().stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Ligne introuvable : id=" + ligneId + " dans la commande " + commandeId));

        boolean estAdmin = demandeur instanceof Administrateur;
        if (!estAdmin && !ligne.getProduit().getProducteur().getId().equals(demandeur.getId())) {
            throw new CommandeAccesRefuseException(
                    "Vous ne pouvez changer le statut que des lignes de vos propres produits.");
        }

        String nouveauStatut = requete.getStatut();
        if (nouveauStatut == null || !STATUTS_VALIDES.contains(nouveauStatut)) {
            throw new IllegalArgumentException(
                    "Statut invalide : \"" + nouveauStatut + "\". Valeurs acceptees : " + STATUTS_VALIDES);
        }

        if (!estAdmin) {
            if ("ANNULEE".equals(ligne.getStatut()) || "LIVREE".equals(ligne.getStatut())) {
                throw new IllegalArgumentException(
                        "Cette ligne est dans un etat final (\"" + ligne.getStatut()
                                + "\") et ne peut plus etre modifiee.");
            }
            if (!"ANNULEE".equals(nouveauStatut)) {
                int indexActuel = ORDRE_STATUTS.indexOf(ligne.getStatut());
                int indexNouveau = ORDRE_STATUTS.indexOf(nouveauStatut);
                if (indexNouveau <= indexActuel) {
                    throw new IllegalArgumentException(
                            "Transition invalide : impossible de passer de \"" + ligne.getStatut()
                                    + "\" a \"" + nouveauStatut + "\" (un Producteur ne peut qu'avancer le statut, "
                                    + "dans l'ordre EN_ATTENTE -> CONFIRMEE -> EN_PREPARATION -> EXPEDIEE -> LIVREE).");
                }
            }
        }

        ligne.setStatut(nouveauStatut);
        Commande enregistree = commandeRepository.save(commande);
        return versReponse(enregistree);
    }

    private CommandeResponse versReponse(Commande commande) {
        List<LigneCommandeResponse> lignes = commande.getLignes().stream()
                .map(ligne -> new LigneCommandeResponse(
                        ligne.getId(),
                        ligne.getProduit().getId(),
                        ligne.getProduit().getNom(),
                        ligne.getQuantite(),
                        ligne.getPrixUnitaire(),
                        ligne.getPrixUnitaire().multiply(java.math.BigDecimal.valueOf(ligne.getQuantite())),
                        ligne.getStatut()
                ))
                .toList();

        Utilisateur acheteur = commande.getAcheteur();
        return new CommandeResponse(
                commande.getId(),
                commande.getDateCommande(),
                commande.getStatut(),
                commande.getMontantTotal(),
                commande.getAdresseLivraison(),
                acheteur.getId(),
                acheteur.getNom() + " " + acheteur.getPrenom(),
                lignes
        );
    }
}
