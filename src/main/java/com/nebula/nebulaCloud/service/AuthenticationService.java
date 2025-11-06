package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.dto.AuthenticationRequest;
import com.nebula.nebulaCloud.dto.AuthenticationResponse;
import com.nebula.nebulaCloud.dto.RegisterRequest;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service layer responsible for handling the business logic of user authentication and registration.
 *
 * This service orchestrates the interaction between the user repository, the password encoder,
 * the JWT service, and Spring's AuthenticationManager to provide secure auth endpoints.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system.
     *
     * This method performs the following steps:
     * 1. Validates that the email is not already in use.
     * 2. Hashes the user's plain-text password for secure storage.
     * 3. Creates and saves the new User entity to the database.
     * 4. Generates a JWT for the newly created user, effectively logging them in.
     *
     * @param request The registration request DTO containing the user's details.
     * @return An AuthenticationResponse containing a valid JWT for the new user.
     * @throws IllegalStateException if a user with the provided email already exists.
     */
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("User with email " + request.getEmail() + " already exists.");
        }

        // Create a new user entity from the request
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Hash the password
                .userType(request.getUserType())
                .planId(request.getPlanId())
                .build();

        // Save the new user to the database
        userRepository.save(user);

        // Generate a JWT for the new user
        String jwtToken = jwtService.generateToken(user);

        // Return the response containing the token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Authenticates an existing user.
     *
     * This method performs the following steps:
     * 1. Delegates the authentication process to Spring's AuthenticationManager,
     *    which will use our configured AuthenticationProvider to validate the credentials.
     * 2. If authentication is successful, it fetches the user details.
     * 3. Generates a new JWT for the authenticated user.
     *
     * @param request The authentication request DTO containing the user's email and password.
     * @return An AuthenticationResponse containing a valid JWT for the session.
     * @throws org.springframework.security.core.AuthenticationException if credentials are invalid.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // The AuthenticationManager will handle the verification of credentials.
        // If the credentials are incorrect, it will throw an AuthenticationException.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If authentication was successful, find the user to generate a token
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication.")); // Should not happen

        // Generate a JWT for the authenticated user
        String jwtToken = jwtService.generateToken(user);

        // Return the response containing the token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
