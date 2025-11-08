package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.dto.OrganizationRequest;
import com.nebula.nebulaCloud.dto.OrganizationResponse;
import com.nebula.nebulaCloud.model.Individual;
import com.nebula.nebulaCloud.model.Organization;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.repository.OrganizationRepository;
import com.nebula.nebulaCloud.repository.IndividualRepository;
import com.nebula.nebulaCloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public OrganizationResponse create(OrganizationRequest request) {
        Individual owner = individualRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));
                User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = Organization.builder()
                .name(request.getName())
                .owner(owner)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        organizationRepository.save(organization);

        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .ownerId(owner.getId())
                .userId(user.getId())
                .createdAt(organization.getCreatedAt())
                .build();
    }

    public List<OrganizationResponse> findAll() {
        return organizationRepository.findAll()
                .stream()
                .map(org -> OrganizationResponse.builder()
                        .id(org.getId())
                        .name(org.getName())
                        .ownerId(org.getOwner().getId())
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
