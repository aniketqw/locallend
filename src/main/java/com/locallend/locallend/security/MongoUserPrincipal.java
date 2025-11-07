package com.locallend.locallend.security;

import com.locallend.locallend.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Bridge between MongoDB User entity and Spring Security UserDetails.
 * Wraps user information for authentication and authorization.
 */
public class MongoUserPrincipal implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final boolean active;
    private final String role;

    public MongoUserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.active = Boolean.TRUE.equals(user.getIsActive());
        this.role = user.getRole() != null ? user.getRole() : "USER";
    }

    public String getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
