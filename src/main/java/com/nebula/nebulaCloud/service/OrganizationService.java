package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.dto.AuthenticationResponse;
import com.nebula.nebulaCloud.dto.OrganizationRequest;
import com.nebula.nebulaCloud.dto.OrganizationResponse;
import com.nebula.nebulaCloud.dto.RegisterRequest;
import com.nebula.nebulaCloud.model.Individual;
import com.nebula.nebulaCloud.model.Organization;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.repository.OrganizationRepository;
import com.nebula.nebulaCloud.repository.IndividualRepository;
import com.nebula.nebulaCloud.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final IndividualRepository individualRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    @Transactional
    public ResponseEntity<OrganizationResponse> create(OrganizationRequest request) {

        // Check if user already exists
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("User with email " + request.getEmail() + " already exists.");
        });

        Individual owner = individualRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));
                //User user = userRepository.findById(request.getUserId())
                //.orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Create a new user entity from the request
        User userOrganization = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userType(request.getUserType())
                .build();

        Organization organization = Organization.builder()
                .name(request.getName())
                .owner(owner)
                .user(userOrganization)
                .createdAt(LocalDateTime.now())
                .build();

        userOrganization.setOrganization(organization);

        // 4. Save the user. Due to CascadeType.ALL, the individual will be saved automatically.
        userRepository.save(userOrganization);
        organizationRepository.save(organization);

        // 5. Generate a JWT for the new user
        String jwtToken = jwtService.generateToken(userOrganization);

        // 6. Create a secure, HttpOnly cookie to store the JWT.
        ResponseCookie jwtCookie = ResponseCookie.from("access_token", jwtToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60) // 24 hours
                .build();

        OrganizationResponse organizationResponse = OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .ownerId(owner.getId())
                .userId(organization.getUser().getId())
                .createdAt(organization.getCreatedAt())
                .build();

        // 8. Return the response with the cookie in the header and user details in the body
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(organizationResponse);


    }

    public List<OrganizationResponse> findAll() {
        return organizationRepository.findAll()
                .stream()
                .map(org -> OrganizationResponse.builder()
                        .id(org.getId())
                        .name(org.getName())
                        .ownerId(org.getOwner().getId())
                        .userId(org.getUser().getId())
                        .createdAt(org.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public OrganizationResponse findById(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .ownerId(org.getOwner().getId())
                .userId(org.getUser().getId())
                .createdAt(org.getCreatedAt())
                .build();
    }

    public void delete(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new RuntimeException("Organization not found");
        }
        organizationRepository.deleteById(id);
    }
}
