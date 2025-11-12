package com.nebula.nebulaCloud.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handler for failed OAuth2 authentication.
 *
 * Redirects to frontend with error information.
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${application.oauth2.redirect-url:http://localhost:3000/auth/callback}")
    private String redirectUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 authentication failed: {}", exception.getMessage());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
            .queryParam("error", "authentication_failed")
            .queryParam("message", exception.getMessage())
            .build()
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

