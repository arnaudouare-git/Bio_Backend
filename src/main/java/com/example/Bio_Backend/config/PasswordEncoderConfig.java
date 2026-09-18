package com.example.bio_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Fournit un PasswordEncoder (BCrypt) pour hasher les mots de passe.
 *
 * Depuis le module Securite (2026-09-18), spring-boot-starter-security est
 * bien present dans le projet (voir SecurityConfig) -- mais Spring Boot ne
 * cree pas de PasswordEncoder par defaut pour autant, ce bean reste donc
 * necessaire. Il est utilise directement par AuthService (hash a
 * l'inscription, verification a la connexion) ; SecurityConfig, lui, n'a
 * pas besoin d'y toucher car AuthService fait deja cette verification
 * lui-meme avant d'emettre un token JWT.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
