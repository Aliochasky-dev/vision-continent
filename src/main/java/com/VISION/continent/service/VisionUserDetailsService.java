package com.VISION.continent.service;

import com.VISION.continent.entity.User;
import com.VISION.continent.repository.UserRepository;
import com.VISION.continent.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisionUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * L'identifiant accepte est indifferemment le telephone OU l'email :
     * le login accepte les deux, et les anciens JWT peuvent porter l'un ou l'autre.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        if (identifiant == null || identifiant.isBlank()) {
            throw new UsernameNotFoundException("Identifiant absent");
        }

        User user = userRepository.findByTelephone(identifiant)
                .or(() -> userRepository.findByEmail(identifiant))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable : " + identifiant));

        return UserPrincipal.from(user);
    }
}
