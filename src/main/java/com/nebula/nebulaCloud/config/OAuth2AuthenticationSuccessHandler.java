package com.nebula.nebulaCloud.config;

import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.service.CustomOAuth2User;
import com.nebula.nebulaCloud.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handler for successful OAuth2 authentication.
 *
 * After successful OAuth2 login:
 * 1. Generates JWT token for the user
 * 2. Sets JWT in HttpOnly cookie
 * 3. Redirects to frontend with profileCompleted status
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Value("${application.oauth2.redirect-url:http://localhost:3000/auth/callback}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        log.info("OAuth2 authentication successful for user: {}", user.getEmail());

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);

        // Set JWT in HttpOnly cookie
        ResponseCookie jwtCookie = ResponseCookie.from("access_token", jwtToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(24 * 60 * 60) // 24 hours
            .build();

        response.addHeader("Set-Cookie", jwtCookie.toString());

        // Get full name with fallback
        String fullName = "";
        if (user.getIndividual() != null && user.getIndividual().getFullName() != null) {
            fullName = user.getIndividual().getFullName().trim();
        }

        // If fullName is still empty, use email username part as fallback
        if (fullName.isEmpty()) {
            String userEmail = user.getEmail();
            fullName = userEmail.contains("@") ? userEmail.substring(0, userEmail.indexOf("@")) : userEmail;
        }

        // Get planId if exists
        Long planId = (user.getPlan() != null) ? user.getPlan().getId() : null;

        // Build redirect URL with ALL user data in JSON format
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(redirectUrl)
            .queryParam("profileCompleted", user.getProfileCompleted())
            .queryParam("email", user.getEmail())
            .queryParam("fullName", fullName)
            .queryParam("userType", user.getUserType().name())
            .queryParam("userId", user.getId());

        // Add planId only if it exists
        if (planId != null) {
            urlBuilder.queryParam("planId", planId);
        }

        String targetUrl = urlBuilder.build().toUriString();

        log.info("Redirecting to: {} with user data - userId: {}, userType: {}, planId: {}",
                 targetUrl, user.getId(), user.getUserType(), planId);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

