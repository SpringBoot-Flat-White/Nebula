package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.model.Individual;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.model.UserType;
import com.nebula.nebulaCloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Custom OAuth2 User Service that handles user authentication via OAuth2 providers (Google, GitHub).
 *
 * This service:
 * 1. Receives user info from OAuth2 provider
 * 2. Checks if user exists in database
 * 3. If new user: creates user with profileCompleted=false
 * 4. If existing user: returns existing user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google" or "github"
        String providerId = oAuth2User.getAttribute("sub") != null
            ? oAuth2User.getAttribute("sub")
            : oAuth2User.getAttribute("id").toString();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // GitHub specific: if email is null, try to get it from login as fallback
        if (email == null && "github".equals(provider)) {
            String login = oAuth2User.getAttribute("login");
            if (login != null) {
                // Use GitHub username as email (will be a noreply email)
                email = login + "@users.noreply.github.com";
                log.warn("GitHub email is private, using fallback: {}", email);
            } else {
                throw new OAuth2AuthenticationException(
                    "Unable to retrieve email from GitHub. Please make sure at least one email is set in your GitHub profile."
                );
            }
        }

        // Validate email is not null
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                "Email is required for authentication. Please ensure your " + provider + " account has a valid email."
            );
        }

        log.info("OAuth2 login attempt - Provider: {}, Email: {}, Name: {}", provider, email, name);

        // Check if user already exists by provider and providerId
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Existing OAuth2 user found: {}", user.getEmail());
        } else {
            // Check if email already exists with different provider
            Optional<User> userByEmail = userRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                throw new OAuth2AuthenticationException(
                    "Email already registered with different authentication method"
                );
            }

            // Create new user
            user = User.builder()
                .email(email)
                .password(null) // No password for OAuth2 users
                .userType(UserType.INDIVIDUAL) // Default type
                .provider(provider)
                .providerId(providerId)
                .profileCompleted(false) // Must complete profile
                .build();

            // Create associated Individual with OAuth2 name (can be "xxgamexx")
            Individual individual = Individual.builder()
                .fullName(name != null ? name : "New User")
                .user(user)
                .build();

            user.setIndividual(individual);
            user = userRepository.save(user);

            log.info("New OAuth2 user created: {}", user.getEmail());
        }

        // Return custom OAuth2User implementation that wraps our User entity
        return new CustomOAuth2User(oAuth2User, user);
    }
}

