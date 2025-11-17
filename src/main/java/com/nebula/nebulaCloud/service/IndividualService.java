package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.dto.IndividualRequest;
import com.nebula.nebulaCloud.dto.IndividualResponse;
import com.nebula.nebulaCloud.exception.ResourceNotFoundException;
import com.nebula.nebulaCloud.model.Individual;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.repository.IndividualRepository;
import com.nebula.nebulaCloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing individual profiles.
 */
@Service
@RequiredArgsConstructor
public class IndividualService {

    private final IndividualRepository individualRepository;
    private final UserRepository userRepository;

    // TODO: Create a new individual profile
    public IndividualResponse create(IndividualRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        Individual individual = Individual.builder()
                .fullName(request.getFullName())
                .user(user)
                .build();

        individualRepository.save(individual);

        return IndividualResponse.builder()
                .id(individual.getId())
                .fullName(individual.getFullName())
                .userId(user.getId())
                .build();
    }

    // TODO: Get all individual profiles
    public List<IndividualResponse> findAll() {
        return individualRepository.findAll()
                .stream()
                .map(i -> IndividualResponse.builder()
                        .id(i.getId())
                        .fullName(i.getFullName())
                        .userId(i.getUser().getId())
                        .build())
                .collect(Collectors.toList());
    }

    // TODO: Get individual profile by ID
    public IndividualResponse findById(Long id) {
        Individual i = individualRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Individual not found with ID: " + id));

        return IndividualResponse.builder()
                .id(i.getId())
                .fullName(i.getFullName())
                .userId(i.getUser().getId())
                .build();
    }

    // TODO: Delete an individual profile
    public void delete(Long id) {
        if (!individualRepository.existsById(id)) {
            throw new ResourceNotFoundException("Individual not found with ID: " + id);
        }
        individualRepository.deleteById(id);
    }
}
