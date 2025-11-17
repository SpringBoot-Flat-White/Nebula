package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Engine;
import com.nebula.nebulaCloud.model.UserDb;
import com.nebula.nebulaCloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing database user entities.
 */
public interface UserDbRepository extends JpaRepository<UserDb, Long> {

    // TODO: Find all database users by user
    List<UserDb> findByUser(User user);

    // TODO: Find database user by username
    Optional<UserDb> findByDbUser(String dbUser);

}
