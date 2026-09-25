package com.example.bio_backend.config;

import com.example.bio_backend.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Point de branchement standard de Spring Security : "etant donne un
 * identifiant (ici l'email), retrouve l'utilisateur correspondant". Utilise
 * par JwtAuthenticationFilter pour recharger l'utilisateur a partir de
 * l'email contenu dans un token recu.
 */
@Service
public class UtilisateurDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurDetailsServiceImpl(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return utilisateurRepository.findByEmail(email)
                .map(UtilisateurDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun compte pour l'email : " + email));
    }
}
