package com.example.bio_backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * S'execute UNE FOIS par requete, AVANT que Spring Security decide
 * d'autoriser ou non l'acces (regles definies dans SecurityConfig). Son
 * role : lire le header "Authorization: Bearer <token>", verifier le token,
 * et si valide, dire a Spring Security "cet utilisateur est authentifie"
 * pour la duree de cette requete (aucune session gardee en memoire --
 * l'API est stateless, voir SecurityConfig).
 *
 * Si le token est absent, invalide ou expire, on ne bloque PAS ici -- on
 * laisse simplement la requete non authentifiee. C'est SecurityConfig qui
 * decidera ensuite si la route demandee necessite une authentification
 * (401/403) ou non (route publique).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtilisateurDetailsServiceImpl utilisateurDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UtilisateurDetailsServiceImpl utilisateurDetailsService) {
        this.jwtService = jwtService;
        this.utilisateurDetailsService = utilisateurDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extraireEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails utilisateur = utilisateurDetailsService.loadUserByUsername(email);

                if (jwtService.estValide(token, utilisateur)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            utilisateur, null, utilisateur.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token mal forme / signature invalide / expire : on ignore, la
            // requete continue simplement non authentifiee (voir javadoc ci-dessus).
        }

        filterChain.doFilter(request, response);
    }
}
