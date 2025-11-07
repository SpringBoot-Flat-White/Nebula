package com.nebula.nebulaCloud.controller;

import com.nebula.nebulaCloud.dto.AuthenticationRequest;
import com.nebula.nebulaCloud.dto.AuthenticationResponse;
import com.nebula.nebulaCloud.dto.RegisterRequest;
import com.nebula.nebulaCloud.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}