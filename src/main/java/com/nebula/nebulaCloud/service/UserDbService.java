package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.dto.UserDbRequest;
import com.nebula.nebulaCloud.dto.UserDbResponse;
import com.nebula.nebulaCloud.exception.ResourceNotFoundException;
import com.nebula.nebulaCloud.model.User;
import com.nebula.nebulaCloud.model.UserDb;
import com.nebula.nebulaCloud.repository.UserDbRepository;
import com.nebula.nebulaCloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDbService {

    private final UserDbRepository userDbRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<UserDbResponse> create(UserDbRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        UserDb userDb = UserDb.builder()
                .dbUser(request.getDbUser())
                //.dbPasswordEnc(passwordEncoder.encode(request.getDbPassword()))
                .user(user)
                .build();

        userDbRepository.save(userDb);

        return ResponseEntity.ok(UserDbResponse.builder()
                .id(userDb.getId())
                .dbUser(userDb.getDbUser())
                .userId(user.getId())
                .build());
    }

    public ResponseEntity<List<UserDbResponse>> getByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<UserDbResponse> list = userDbRepository.findByUser(user).stream()
                .map(db -> UserDbResponse.builder()
                        .id(db.getId())
                        .dbUser(db.getDbUser())
                        .userId(user.getId())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    public ResponseEntity<Void> delete(Long id) {
        userDbRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
