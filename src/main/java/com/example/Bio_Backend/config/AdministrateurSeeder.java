package com.example.bio_backend.config;

import com.example.bio_backend.entity.Administrateur;
import com.example.bio_backend.repository.AdministrateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cree un compte Administrateur de developpement au demarrage de
 * l'application, UNIQUEMENT s'il n'en existe encore aucun en base (voir la
 * garde dans run()).
 *
 * Pourquoi ce seeder existe : il n'y a aucun moyen de creer un compte
 * ADMINISTRATEUR via l'API publique -- POST /api/auth/inscription
 * n'accepte que PRODUCTEUR ou CLIENT (voir AuthService.inscrire()), et
 * c'est voulu : on ne veut pas qu'un utilisateur puisse s'auto-attribuer
 * les droits admin en passant simplement "role": "ADMINISTRATEUR" dans sa
 * requete d'inscription. Il faut donc un autre mecanisme pour obtenir un
 * premier compte admin a tester (routes /api/admin/**, voir SecurityConfig
 * et AdminController) : ce seeder de dev en est un.
 *
 * ATTENTION -- NE JAMAIS UTILISER TEL QUEL EN PRODUCTION : identifiants
 * fixes, en clair dans le code source. A supprimer (ou a proteger derriere
 * un profil Spring "dev" / des variables d'environnement) avant tout
 * deploiement reel.
 *
 * Identifiants de dev :
 *   email        : admin@bioconversion.com
 *   mot de passe : admin1234
 */
@Component
public class AdministrateurSeeder implements CommandLineRunner {

    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministrateurSeeder(AdministrateurRepository administrateurRepository,
                                 PasswordEncoder passwordEncoder) {
        this.administrateurRepository = administrateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Garde : on ne cree le compte de dev QUE si aucun admin n'existe
        // deja en base -- sinon on en recreerait un a chaque redemarrage.
        if (administrateurRepository.count() > 0) {
            return;
        }

        Administrateur admin = new Administrateur();
        admin.setNom("Admin");
        admin.setPrenom("BioConversion");
        admin.setEmail("admin@bioconversion.com");
        admin.setMotDePasse(passwordEncoder.encode("admin1234"));
        admin.setTelephone("70000000");
        admin.setStatutVerificationCnib("VERIFIE");

        administrateurRepository.save(admin);

        System.out.println("[AdministrateurSeeder] Compte Administrateur de dev cree : "
                + "admin@bioconversion.com / admin1234 (NE JAMAIS UTILISER EN PRODUCTION)");
    }
}
