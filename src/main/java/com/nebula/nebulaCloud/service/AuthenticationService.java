package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.dto.AuthenticationRequest;
import com.nebula.nebulaCloud.dto.AuthenticationResponse;
import com.nebula.nebulaCloud.dto.CompleteProfileRequest;
import com.nebula.nebulaCloud.dto.CompleteProfileResponse;
import com.nebula.nebulaCloud.dto.RegisterRequest;
import com.nebula.nebulaCloud.model.Individual;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer responsible for handling the business logic of user authentication and registration.
 *
 * This service orchestrates the interaction between the user repository, the password encoder,
 * the JWT service, and Spring's AuthenticationManager to provide secure auth endpoints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    // IndividualRepository no es necesario aquí si se usa Cascade.ALL

    /**
     * Registers a new user in the system and authenticates them.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *     <li>Validates that the email is not already in use.</li>
     *     <li>Hashes the user's password for secure storage.</li>
     *     <li>Creates and saves the new User and its associated Individual entity in a single transaction.</li>
     *     <li>Generates a JWT for the newly created user.</li>
     *     <li>Sets the JWT in a secure, HttpOnly cookie for session management.</li>
     * </ol>
     *
     * @param request The registration request DTO containing the user's details.
     * @return A {@link ResponseEntity} with user details in the body and a 'Set-Cookie' header containing the auth token.
     * @throws IllegalStateException if a user with the provided email already exists.
     */
    @Transactional
    public ResponseEntity<AuthenticationResponse> register(RegisterRequest request) {
        // Check if user already exists
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("User with email " + request.getEmail() + " already exists.");
        });

        // 1. Create a new user entity from the request
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userType(request.getUserType())
                .provider("local") // Traditional registration
                .profileCompleted(true) // Profile is complete on traditional registration
                .build();

        // 2. Create the associated Individual entity
        Individual individual = Individual.builder()
                .fullName(request.getFullName())
                .user(user) // Link Individual to User
                .build();

        // 3. Complete the bidirectional link
        user.setIndividual(individual);

        // 4. Save the user. Due to CascadeType.ALL, the individual will be saved automatically.
        userRepository.save(user);

        // 5. Generate a JWT for the new user
        String jwtToken = jwtService.generateToken(user);

        // 6. Create a secure, HttpOnly cookie to store the JWT.
        ResponseCookie jwtCookie = ResponseCookie.from("access_token", jwtToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60) // 24 hours
                .build();

        // 7. Build the response body DTO with user details (without the token)
        String planName = (user.getPlan() != null) ? user.getPlan().getName() : "FREE";
        
        AuthenticationResponse responseBody = AuthenticationResponse.builder()
                .email(user.getEmail())
                .fullName(individual.getFullName())
                .userType(user.getUserType())
                .plan(planName)
                .build();

        // 8. Return the response with the cookie in the header and user details in the body
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(responseBody);
    }

    /**
     * Authenticates an existing user based on their credentials and returns their details.
     *
     * <p>This method orchestrates the authentication process by performing the following steps:</p>
     * <ol>
     *     <li>Delegates credential validation (email and password) to Spring Security's {@link AuthenticationManager}.</li>
     *     <li>If authentication is successful, it fetches the complete {@link User} entity from the database.</li>
     *     <li>Generates a JSON Web Token (JWT) for the authenticated user to manage their session.</li>
     *     <li>Embeds the generated JWT into a secure, <strong>HttpOnly</strong> cookie. This cookie is automatically handled by the browser
     *     and is not accessible to client-side scripts, mitigating the risk of XSS attacks.</li>
     *     <li>Constructs and returns a response body containing non-sensitive user details (email, full name, user type).</li>
     * </ol>
     *
     * @param request The {@link AuthenticationRequest} DTO containing the user's email and password.
     * @return A {@link ResponseEntity} where the body contains an {@link AuthenticationResponse} with user details,
     *         and a 'Set-Cookie' header contains the JWT as a secure, HttpOnly cookie. The token itself is excluded from the response body.
     * @throws AuthenticationException if the credentials provided in the request are invalid.
     * @throws IllegalStateException if the user is successfully authenticated but cannot be found in the repository,
     *         which indicates a potential data consistency issue.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        // 1. The AuthenticationManager will handle the verification of credentials.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. If successful, find the user to generate a token and response.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after successful authentication."));

        // 3. Get the full name from the associated Individual entity.
        String fullName = (user.getIndividual() != null) ? user.getIndividual().getFullName() : "";

        // 4. Generate a JWT for the authenticated user.
        String jwtToken = jwtService.generateToken(user);

        // 5. Create a secure, HttpOnly cookie to store the JWT.
        ResponseCookie jwtCookie = ResponseCookie.from("access_token", jwtToken) // Key-value pair
                .httpOnly(true)                                         // Prevents access from JavaScript (XSS protection)
                .secure(true)                                           // Ensures the cookie is sent only over HTTPS
                .path("/")                                              // The cookie is available for all paths in the domain
                .maxAge(24 * 60 * 60)                                   // Sets cookie expiration (e.g., 24 hours in seconds)
                .build();

        // 6. Build the response body DTO with user details, excluding the token.
        String planName = (user.getPlan() != null) ? user.getPlan().getName() : "FREE";
        
        AuthenticationResponse responseBody = AuthenticationResponse.builder()
                .email(user.getEmail())
                .fullName(fullName)
                .userType(user.getUserType())
                .plan(planName)
                .userId(user.getId())
                .planId(user.getPlan().getId())
                .build();

        // 7. Return the final ResponseEntity, adding the cookie to the headers and the DTO to the body.
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(responseBody);
    }

    /**
     * Logs out the current user by clearing the authentication cookie.
     *
     * <p>This method creates a new {@link ResponseCookie} with the same name ('access_token')
     * as the authentication cookie, but with a max age of 0. When sent to the browser,
     * this instruction effectively overwrites and deletes the existing cookie,
     * terminating the user's session from the client's perspective.</p>
     *
     * @return A {@link ResponseEntity} with a 'Set-Cookie' header that clears the
     *         authentication token and a success message in the body.
     */
    public ResponseEntity<String> logout() {
        // Create a cookie with the same name, but with Max-Age = 0 to invalidate it
        ResponseCookie cookie = ResponseCookie.from("access_token", "") // Value can be empty
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout successful!");
    }

    /**
     * Completes the user profile after OAuth2 registration.
     *
     * This method is called when a user logs in via OAuth2 for the first time
     * and needs to provide their real information (full name, etc.).
     *
     * This should only be called once per OAuth2 user.
     *
     * @param email The email of the user completing their profile
     * @param request The profile completion request with user's real data
     * @return A {@link ResponseEntity} with the updated user information
     */
    @Transactional
    public ResponseEntity<CompleteProfileResponse> completeProfile(String email, CompleteProfileRequest request) {
        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Verify that profile is not already completed
        if (Boolean.TRUE.equals(user.getProfileCompleted())) {
            throw new IllegalStateException("Profile already completed");
        }

        // Update Individual with real name
        Individual individual = user.getIndividual();
        if (individual != null) {
            individual.setFullName(request.getFullName());
        } else {
            // Create Individual if it doesn't exist (edge case)
            individual = Individual.builder()
                    .fullName(request.getFullName())
                    .user(user)
                    .build();
            user.setIndividual(individual);
        }

        // Mark profile as completed
        user.setProfileCompleted(true);

        // Save changes
        userRepository.save(user);

        log.info("Profile completed for user: {}", user.getEmail());

        // Build response
        CompleteProfileResponse response = CompleteProfileResponse.builder()
                .email(user.getEmail())
                .fullName(individual.getFullName())
                .profileCompleted(true)
                .message("Profile completed successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Gets the current authenticated user's information including updated plan.
     *
     * This method retrieves fresh data from the database to ensure
     * the frontend has the latest information, especially after payment completion.
     *
     * @param authentication The Spring Security authentication object
     * @return A {@link ResponseEntity} with the current user information
     */
    public ResponseEntity<AuthenticationResponse> getCurrentUser(
            org.springframework.security.core.Authentication authentication
    ) {
        // Get the authenticated user from the principal
        User user = (User) authentication.getPrincipal();
        
        // Fetch fresh data from database to get updated plan
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        // Get the full name from Individual or Organization
        String fullName = "";
        if (freshUser.getIndividual() != null) {
            fullName = freshUser.getIndividual().getFullName();
        } else if (freshUser.getOrganization() != null) {
            fullName = freshUser.getOrganization().getName();
        }
        
        // Get the plan name (default to FREE if no plan assigned)
        String planName = "FREE";
        if (freshUser.getPlan() != null) {
            planName = freshUser.getPlan().getName();
        }
        
        log.info("Fetched current user data: {} - Plan: {}", freshUser.getEmail(), planName);
        
        // Build response with updated information
        AuthenticationResponse response = AuthenticationResponse.builder()
                .email(freshUser.getEmail())
                .fullName(fullName)
                .userType(freshUser.getUserType())
                .plan(planName)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Gets the current authenticated user's information from JWT cookie.
     *
     * This method retrieves the authenticated user from the security context
     * (populated by JwtAuthenticationFilter from the JWT cookie)
     * and returns their information in AuthenticationResponse format.
     *
     * @return A {@link ResponseEntity} with the current user's information
     * @throws IllegalStateException if no user is authenticated or user not found
     */
    @Transactional(readOnly = true)
    public ResponseEntity<AuthenticationResponse> getCurrentUser() {
        // Get current authenticated user from security context
        org.springframework.security.core.Authentication authentication =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No authenticated user found");
        }

        // Get user email from authentication (set by JwtAuthenticationFilter)
        String email = authentication.getName();

        // Fetch user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

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

        // Build response
        AuthenticationResponse response = AuthenticationResponse.builder()
                .email(user.getEmail())
                .fullName(fullName)
                .userType(user.getUserType())
                .userId(user.getId())
                .planId(user.getPlan() != null ? user.getPlan().getId() : null)
                .build();

        return ResponseEntity.ok(response);
    }


}