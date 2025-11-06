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
     * @return A ResponseEntity containing the AuthenticationResponse with a new JWT
     *         upon successful registration. Returns an HTTP 200 OK status.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        // Delegate the registration logic to the service layer
        AuthenticationResponse response = authenticationService.register(request);
        // Return the response with an OK status
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the user authentication (login) request.
     * This endpoint is also publicly accessible.
     *
     * @param request The authentication request DTO, containing user credentials. The @Valid
     *                annotation triggers validation.
     * @return A ResponseEntity containing the AuthenticationResponse with a new JWT
     *         for the user's session. Returns an HTTP 200 OK status.
     *         If authentication fails, the AuthenticationService will throw an exception,
     *         which will be handled by a global exception handler to return an appropriate
     *         error status (e.g., 401 Unauthorized).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        // Delegate the authentication logic to the service layer
        AuthenticationResponse response = authenticationService.authenticate(request);
        // Return the response with an OK status
        return ResponseEntity.ok(response);
    }
}
