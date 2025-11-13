package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.dto.AuthenticationRequest;
import com.nebula.nebulaCloud.dto.AuthenticationResponse;
import com.nebula.nebulaCloud.dto.RegisterRequest;
import com.nebula.nebulaCloud.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling authentication-related endpoints.
 *
 * This controller exposes the registration and authentication functionalities
 * as public API endpoints. It follows the standard practice of grouping related
 * endpoints under a common base path, in this case, "/api/v1/auth".
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Handles the user registration request.
     * This endpoint is publicly accessible as defined in SecurityConfig.
     *
     * @param request The registration request DTO, containing user details. The @Valid
     *                annotation triggers validation based on the constraints defined in
     *                the RegisterRequest class.
     * @return A ResponseEntity containing user details in the body and a secure, HttpOnly cookie
     *         with the JWT in the headers.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        // Delegate the logic to the service layer and return its response directly.
        // The service is responsible for building the full ResponseEntity, including headers.
        return authenticationService.register(request);
    }

    /**
     * Handles the user authentication (login) request.
     * This endpoint is also publicly accessible.
     *
     * @param request The authentication request DTO, containing user credentials.
     * @return A ResponseEntity containing user details in the body and a secure, HttpOnly cookie
     *         with the JWT in the headers for the user's session.
     *         If authentication fails, an exception will be thrown and handled globally.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        // Delegate the logic to the service layer and return its response directly.
        // This preserves the HttpOnly cookie set by the service.
        return authenticationService.authenticate(request);
    }

    /**
     * Handles the user logout request.
     *
     * <p>This endpoint invalidates the user's session by instructing the browser to clear
     * the authentication cookie. It is a protected endpoint, meaning a user must be
     * logged in to be able to log out.</p>
     *
     * @return A ResponseEntity confirming the successful logout.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return authenticationService.logout();
    }

    /**
     * Completes the user profile after OAuth2 registration.
     *
     * This endpoint is called once after a user's first OAuth2 login
     * to allow them to provide their real information.
     *
     * @param email The email of the authenticated user (from JWT or session)
     * @param request The profile completion data
     * @return A ResponseEntity with the updated profile information
     */
    @PostMapping("/complete-profile")
    public ResponseEntity<com.nebula.nebulaCloud.dto.CompleteProfileResponse> completeProfile(
            @RequestParam String email,
            @Valid @RequestBody com.nebula.nebulaCloud.dto.CompleteProfileRequest request
    ) {
        return authenticationService.completeProfile(email, request);
    }

    /**
     * Gets the current authenticated user's information including updated plan.
     *
     * This endpoint is used by the frontend to refresh user data,
     * especially after payment completion.
     *
     * @param authentication The authenticated user from Spring Security
     * @return A ResponseEntity with the current user information
     */
    @GetMapping("/me")
    public ResponseEntity<AuthenticationResponse> getCurrentUser(
            org.springframework.security.core.Authentication authentication
    ) {
        return authenticationService.getCurrentUser(authentication);
    }

    /**
     * Test endpoint to verify OAuth2 callback is working.
     * This helps debug frontend issues after OAuth2 redirect.
     *
     * @return A simple success message
     */
    @GetMapping("/oauth2-test")
    public ResponseEntity<String> oauth2Test() {
        return ResponseEntity.ok("OAuth2 backend is working! You can see this message.");
    }
}