package com.example.bio_backend.service;

import com.example.bio_backend.config.JwtService;
import com.example.bio_backend.config.UtilisateurDetailsImpl;
import com.example.bio_backend.dto.AuthResponse;
import com.example.bio_backend.dto.LoginRequest;
import com.example.bio_backend.dto.RegisterRequest;
import com.example.bio_backend.dto.RoleUtilisateur;
import com.example.bio_backend.entity.Client;
import com.example.bio_backend.entity.Notification;
import com.example.bio_backend.entity.Producteur;
import com.example.bio_backend.entity.Utilisateur;
import com.example.bio_backend.exception.EmailDejaUtiliseException;
import com.example.bio_backend.exception.IdentifiantsInvalidesException;
import com.example.bio_backend.repository.ClientRepository;
import com.example.bio_backend.repository.NotificationRepository;
import com.example.bio_backend.repository.ProducteurRepository;
import com.example.bio_backend.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logique metier de l'inscription et de la connexion.
 *
 * Pourquoi une couche Service et pas tout dans le Controller ? Le Controller
 * ne doit s'occuper QUE du HTTP (recevoir la requete, renvoyer la reponse).
 * Toute la logique -- verifier l'unicite de l'email, hasher le mot de passe,
 * creer le compte -- vit ici, pour pouvoir etre reutilisee ailleurs (ex :
 * import en masse) sans dependre du HTTP.
 *
 * v3 du modele (2026-09-11) : DEUX roles publics (voir RoleUtilisateur).
 *   - PRODUCTEUR : produit, vend ET achete. Formation (hors plateforme) +
 *     verification de la piece d'identite (CNIB) par un Administrateur
 *     obligatoires avant activation -- voir AdminService.
 *   - CLIENT : achete SEULEMENT (jamais de vente ni de production).
 *     Inscription libre et immediate : ni formation, ni CNIB. Son compte
 *     passe directement au statut "VERIFIE" (voir inscrireClient) pour que
 *     le futur controle d'acces cote achat (statutVerificationCnib ==
 *     "VERIFIE") fonctionne de la meme facon pour les deux roles.
 *
 * Module Securite (2026-09-18) : chaque reponse (inscription ET connexion)
 * contient desormais un token JWT, genere via JwtService. Note qu'on ne
 * verifie PAS le mot de passe via Spring Security's AuthenticationManager --
 * on garde notre propre verification manuelle (passwordEncoder.matches())
 * ci-dessous, et on se contente d'emettre un token une fois cette
 * verification passee. C'est SecurityConfig qui, lui, protege les routes en
 * validant ce token sur les requetes suivantes.
 */
@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProducteurRepository producteurRepository;
    private final ClientRepository clientRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                        ProducteurRepository producteurRepository,
                        ClientRepository clientRepository,
                        NotificationRepository notificationRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.producteurRepository = producteurRepository;
        this.clientRepository = clientRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse inscrire(RegisterRequest requete) {
        // 1. Un seul email par compte, tous roles confondus (verifie via le
        //    repository parent, qui voit Producteur ET Client grace au
        //    JOINED inheritance).
        utilisateurRepository.findByEmail(requete.getEmail()).ifPresent(u -> {
            throw new EmailDejaUtiliseException(requete.getEmail());
        });

        if (requete.getRole() == null) {
            throw new IllegalArgumentException(
                    "Le champ 'role' est obligatoire : PRODUCTEUR ou CLIENT.");
        }

        // 2. On ne stocke JAMAIS le mot de passe en clair.
        String motDePasseHashe = passwordEncoder.encode(requete.getMotDePasse());

        if (requete.getRole() == RoleUtilisateur.PRODUCTEUR) {
            return inscrireProducteur(requete, motDePasseHashe);
        }
        return inscrireClient(requete, motDePasseHashe);
    }

    /**
     * Un Producteur reste soumis a la formation + verification CNIB : son
     * compte est cree EN_ATTENTE et ne devient actif qu'apres activation par
     * un Administrateur (voir AdminService.activerCompte).
     */
    private AuthResponse inscrireProducteur(RegisterRequest requete, String motDePasseHashe) {
        if (requete.getDocumentIdentiteRef() == null || requete.getDocumentIdentiteRef().isBlank()) {
            throw new IllegalArgumentException(
                    "Le champ 'documentIdentiteRef' (piece d'identite CNIB) est obligatoire pour s'inscrire en tant que Producteur.");
        }

        Producteur producteur = new Producteur();
        producteur.setNom(requete.getNom());
        producteur.setPrenom(requete.getPrenom());
        producteur.setEmail(requete.getEmail());
        producteur.setMotDePasse(motDePasseHashe);
        producteur.setTelephone(requete.getTelephone());
        producteur.setDocumentIdentiteRef(requete.getDocumentIdentiteRef());
        producteur.setLatitude(requete.getLatitude());
        producteur.setLongitude(requete.getLongitude());

        Producteur enregistre = producteurRepository.save(producteur);

        // statutVerificationCnib est deja mis a "EN_ATTENTE" automatiquement
        // par @PrePersist dans Utilisateur. Le compte ne devient utilisable
        // (achat, vente) qu'apres : (a) avoir suivi la formation obligatoire
        // -- enregistree par un admin via AdminService.enregistrerParticipant
        // -- puis (b) verification de la piece d'identite par un admin via
        // AdminService.activerCompte.
        return versReponse(enregistre);
    }

    /**
     * Un Client s'inscrit librement et immediatement : ni formation, ni
     * CNIB. On force statutVerificationCnib a "VERIFIE" des la creation (au
     * lieu du "EN_ATTENTE" par defaut de @PrePersist) pour que le futur
     * controle d'acces cote achat (statutVerificationCnib == "VERIFIE")
     * fonctionne de la meme facon, que l'acheteur soit un Producteur deja
     * verifie ou un Client -- toujours verifie par definition.
     */
    private AuthResponse inscrireClient(RegisterRequest requete, String motDePasseHashe) {
        Client client = new Client();
        client.setNom(requete.getNom());
        client.setPrenom(requete.getPrenom());
        client.setEmail(requete.getEmail());
        client.setMotDePasse(motDePasseHashe);
        client.setTelephone(requete.getTelephone());
        client.setStatutVerificationCnib("VERIFIE");

        Client enregistre = clientRepository.save(client);

        Notification bienvenue = new Notification();
        bienvenue.setUtilisateur(enregistre);
        bienvenue.setDestinataire(enregistre.getEmail());
        bienvenue.setCanal("EMAIL");
        bienvenue.setMessage(
                "Bienvenue sur BioConversion ! Votre compte est actif, vous pouvez acheter des larves des maintenant.");
        notificationRepository.save(bienvenue);

        return versReponse(enregistre);
    }

    public AuthResponse connecter(LoginRequest requete) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(requete.getEmail())
                .orElseThrow(IdentifiantsInvalidesException::new);

        if (!passwordEncoder.matches(requete.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new IdentifiantsInvalidesException();
        }

        // NB : la connexion reste possible meme si le compte Producteur est
        // encore EN_ATTENTE (il doit pouvoir se connecter pour voir son
        // statut) ; un Client, lui, est toujours VERIFIE des la creation.
        // C'est au moment des actions sensibles (achat, vente, paiement) que
        // statutVerificationCnib doit etre verifie -- deja cable dans
        // ProduitService/CommandeService/PaiementService.
        return versReponse(utilisateur);
    }

    /**
     * Determine le role a partir du type reel de l'objet (grace a
     * l'heritage JOINED, "utilisateur" est en memoire une vraie instance de
     * Producteur, Client ou Administrateur, meme si la variable est typee
     * Utilisateur) et genere le token JWT correspondant.
     */
    private AuthResponse versReponse(Utilisateur utilisateur) {
        String role;
        if (utilisateur instanceof Producteur) {
            role = "PRODUCTEUR";
        } else if (utilisateur instanceof Client) {
            role = "CLIENT";
        } else {
            role = "ADMINISTRATEUR";
        }

        String token = jwtService.genererToken(new UtilisateurDetailsImpl(utilisateur));

        return new AuthResponse(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                role,
                utilisateur.getStatutVerificationCnib(),
                token
        );
    }
}
