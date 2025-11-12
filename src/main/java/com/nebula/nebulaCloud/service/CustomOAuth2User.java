package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Custom OAuth2User implementation that wraps both the OAuth2User from the provider
 * and our internal User entity.
 *
 * This allows us to access both OAuth2 attributes and our database User entity
 * throughout the authentication flow.
 */
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final User user;

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oAuth2User.getName();
    }

    /**
     * Get the internal User entity
     */
    public User getUser() {
        return user;
    }
}

