package com.example.bio_backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Genere et valide les jetons JWT (JSON Web Token).
 *
 * Rappel du principe : un JWT est un texte signe cryptographiquement (pas
 * chiffre -- son contenu est lisible par n'importe qui, mais personne ne
 * peut le modifier sans casser la signature). On y met l'email de
 * l'utilisateur (le "subject") + une date d'expiration. Le serveur n'a donc
 * PAS besoin de garder une session en memoire : a chaque requete, il
 * revalide juste la signature du token recu -- c'est ce qu'on appelle une
 * API "stateless" (sans etat cote serveur).
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey cleSecrete() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String genererToken(UserDetails utilisateur) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + expirationMs);

        return Jwts.builder()
                .subject(utilisateur.getUsername()) // ici : l'email, voir UtilisateurDetailsImpl.getUsername()
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(cleSecrete())
                .compact();
    }

    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    /** true si le token est signe correctement, pas expire, et correspond bien a cet utilisateur. */
    public boolean estValide(String token, UserDetails utilisateur) {
        String email = extraireEmail(token);
        return email.equals(utilisateur.getUsername()) && !estExpire(token);
    }

    private boolean estExpire(String token) {
        return extraireClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraireClaim(String token, Function<Claims, T> resolveur) {
        Claims claims = Jwts.parser()
                .verifyWith(cleSecrete())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolveur.apply(claims);
    }
}
