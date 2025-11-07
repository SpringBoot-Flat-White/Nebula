package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.dto.AuthenticationRequest;
import com.nebula.nebulaCloud.dto.AuthenticationResponse;
import com.nebula.nebulaCloud.dto.RegisterRequest;
import com.nebula.nebulaCloud.model.Individual;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    // IndividualRepository no es necesario aquí si se usa Cascade.ALL

    /**
     * Registers a new user in the system.
     *
     * This method performs the following steps:
     * 1. Validates that the email is not already in use.
     * 2. Hashes the user's plain-text password for secure storage.
     * 3. Creates and saves the new User and its associated Individual entity in a single transaction.
     * 4. Generates a JWT for the newly created user, effectively logging them in.
     *
     * @param request The registration request DTO containing the user's details.
     * @return An AuthenticationResponse containing a valid JWT and user details for the new user.
     * @throws IllegalStateException if a user with the provided email already exists.
     */
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if user already exists
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("User with email " + request.getEmail() + " already exists.");
        });

        // 1. Create a new user entity from the request
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userType(request.getUserType())
                .build();

        // 2. Create the associated Individual entity
        Individual individual = Individual.builder()
                .fullName(request.getFullName())
                .user(user) // Link Individual to User
                .build();

        // 3. Complete the bidirectional link
        user.setIndividual(individual);

        // 4. Save the new user. Due to CascadeType.ALL, the individual will be saved automatically.
        userRepository.save(user);

        // Generate a JWT for the new user
        String jwtToken = jwtService.generateToken(user);

        // Return the full response containing the token and user info
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .fullName(individual.getFullName())
                .userType(user.getUserType())
                .build();
    }

    /**
     * Authenticates an existing user.
     *
     * This method performs the following steps:
     * 1. Delegates the authentication process to Spring's AuthenticationManager.
     * 2. If authentication is successful, it fetches the user details along with their profile.
     * 3. Generates a new JWT for the authenticated user.
     *
     * @param request The authentication request DTO containing the user's email and password.
     * @return An AuthenticationResponse containing a valid JWT and user details for the session.
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

        // Get the full name from the associated Individual entity
        String fullName = (user.getIndividual() != null) ? user.getIndividual().getFullName() : "";

        // Generate a JWT for the authenticated user
        String jwtToken = jwtService.generateToken(user);

        // Return the full response containing the token and user info
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .fullName(fullName)
                .userType(user.getUserType())
                .build();
    }
}