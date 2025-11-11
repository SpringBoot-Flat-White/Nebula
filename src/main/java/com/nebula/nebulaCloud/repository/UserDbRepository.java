package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.UserDb;
import com.nebula.nebulaCloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDbRepository extends JpaRepository<UserDb, Long> {

    List<UserDb> findByUser(User user);

    Optional<UserDb> findByDbUser(String dbUser);

}
