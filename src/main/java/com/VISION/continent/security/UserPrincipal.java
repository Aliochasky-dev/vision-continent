package com.VISION.continent.security;

import com.VISION.continent.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Principal applicatif : porte l'id, le telephone, l'email et le role de l'utilisateur.
 *
 * getUsername() renvoie le TELEPHONE, qui est l'identifiant metier utilise comme
 * "subject" des JWT et par les controllers (commentaires, likes, ...).
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String telephone;
    private final String email;
    private final String password;
    private final String role;                       // toujours prefixe "ROLE_"
    private final Set<GrantedAuthority> authorities;
    private final boolean accountNonLocked;
    private final boolean enabled;

    private UserPrincipal(Long id, String telephone, String email, String password,
                          String role, Set<GrantedAuthority> authorities,
                          boolean accountNonLocked, boolean enabled) {
        this.id = id;
        this.telephone = telephone;
        this.email = email;
        this.password = password;
        this.role = role;
        this.authorities = authorities;
        this.accountNonLocked = accountNonLocked;
        this.enabled = enabled;
    }

    public static UserPrincipal from(User user) {
        String rawRole = user.getRole() != null ? user.getRole().name() : "USER";
        String withPrefix = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
        String withoutPrefix = rawRole.startsWith("ROLE_") ? rawRole.substring(5) : rawRole;

        // Les deux formes sont exposees pour que hasRole('ADMIN') ET
        // hasAuthority('ADMIN') / hasAuthority('ROLE_ADMIN') fonctionnent.
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(List.of(
                new SimpleGrantedAuthority(withPrefix),
                new SimpleGrantedAuthority(withoutPrefix)
        ));

        return new UserPrincipal(
                user.getId(),
                user.getTelephone(),
                user.getEmail(),
                user.getPassword(),
                withPrefix,
                authorities,
                user.getStatut() != User.Statut.BANNI,
                user.getStatut() != User.Statut.SUSPENDU
        );
    }

    public Long getId() {
        return id;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    /** Role normalise, toujours prefixe "ROLE_" (ex: ROLE_ADMIN). */
    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return telephone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
