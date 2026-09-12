package com.example.bio_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Fournit un PasswordEncoder (BCrypt) pour hasher les mots de passe.
 *
 * Depend de spring-security-crypto (ajoute dans pom.xml). Volontairement PAS
 * spring-boot-starter-security complet : celui-ci activerait automatiquement
 * une authentification HTTP Basic sur TOUTES les routes existantes, ce qu'on
 * ne veut pas encore (Spring Security complet = etape "Plus tard").
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
