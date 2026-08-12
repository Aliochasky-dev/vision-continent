package com.VISION.continent.service;

import com.VISION.continent.entity.User;
import com.VISION.continent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisionUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        User user = userRepository.findByTelephone(identifiant)
                .or(() -> userRepository.findByEmail(identifiant))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable : " + identifiant));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getTelephone())
                .password(user.getPassword())
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountLocked(user.getStatut() == User.Statut.BANNI)
                .disabled(user.getStatut() == User.Statut.SUSPENDU)
                .build();
    }
}