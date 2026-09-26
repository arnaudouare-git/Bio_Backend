package com.example.bio_backend.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;

/**
 * Point CENTRAL de la securite de l'API : QUI a le droit d'appeler QUEL
 * endpoint. Volontairement tout regroupe ici plutot qu'eparpille en
 * annotations sur chaque controleur, pour que toute l'equipe puisse voir
 * d'un coup d'oeil les regles d'acces (voir aussi le tableau recapitulatif
 * dans la doc du projet, Module_Securite_....md).
 *
 * NB : on n'utilise PAS AuthenticationManager/AuthenticationProvider ici --
 * AuthService fait deja lui-meme la verification du mot de passe (via
 * PasswordEncoder.matches(), voir AuthService.connecter()) au moment du
 * login. Cette classe ne s'occupe que de PROTEGER les routes APRES la
 * connexion, via le token JWT deja emis.
 *
 * CORS (ajoute le 2026-09-26) : le frontend Angular (ng serve, port 4200)
 * et le backend (port 8080) sont deux origines differentes du point de vue
 * du navigateur. Sans configuration CORS explicite, le navigateur bloque
 * silencieusement toutes les reponses de l'API -- meme si le serveur les
 * traite correctement. Necessaire des qu'un frontend web appelle cette API
 * depuis un port different.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Origines autorisees a appeler l'API. localhost:4200 = ng serve (dev).
        // A completer avec l'URL de prod le jour ou le frontend est deploye.
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API stateless (pas de session HTTP, pas de cookie) -> pas besoin de protection CSRF.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 0. Requetes preflight CORS (OPTIONS) : toujours publiques, sinon le
                        // navigateur n'obtient jamais l'autorisation avant d'envoyer la vraie requete.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 1. Inscription/connexion : forcement public (on n'a pas encore de token avant !)
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. Lectures publiques : catalogue marketplace + monitoring IoT en lecture seule.
                        .requestMatchers(HttpMethod.GET,
                                "/api/produits/**", "/api/serres/**",
                                "/api/capteurs/**", "/api/mesures/**", "/api/alertes/**",
                                "/api/actionneurs/**", "/api/avis/**")
                        .permitAll()

                        // 3. Reserve Administrateur.
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRATEUR")
                        // Module Litige (Tier 2, 2026-09-24) : lister tous les litiges et les
                        // trancher est reserve a l'Admin ; ouvrir/consulter SON PROPRE litige
                        // reste sous la regle 5 (n'importe quel compte connecte), avec
                        // verification de propriete faite cote service (LitigeService).
                        .requestMatchers(HttpMethod.GET, "/api/litiges").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/litiges/**").hasRole("ADMINISTRATEUR")

                        // 4. Gestion du catalogue et de l'infrastructure IoT : reserve Producteur (ou Admin).
                        .requestMatchers(HttpMethod.POST, "/api/produits").hasAnyRole("PRODUCTEUR", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/produits/**").hasAnyRole("PRODUCTEUR", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/produits/**").hasAnyRole("PRODUCTEUR", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/serres", "/api/capteurs", "/api/mesures")
                        .hasAnyRole("PRODUCTEUR", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/alertes/**").hasAnyRole("PRODUCTEUR", "ADMINISTRATEUR")
                        // Module Actionneur (ajoute le 2026-09-24, retour client) : meme regle
                        // que les autres ecritures IoT (pilotage manuel reserve Producteur/Admin).
                        .requestMatchers(HttpMethod.PUT, "/api/actionneurs/**").hasAnyRole("PRODUCTEUR", "ADMINISTRATEUR")

                        // 5. Tout le reste (Commandes, Paiements...) : n'importe quel compte connecte,
                        //    Client inclus -- ce sont eux qui achetent.
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // Meme format JSON que GlobalExceptionHandler (timestamp/status/message),
                        // pour ne pas surprendre avec la page blanche par defaut de Spring Security.
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"timestamp\":\"" + Instant.now()
                                    + "\",\"status\":401,\"message\":\"Authentification requise "
                                    + "(token manquant, invalide ou expire).\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"timestamp\":\"" + Instant.now()
                                    + "\",\"status\":403,\"message\":\"Acces refuse : "
                                    + "role insuffisant pour cette action.\"}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
