package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Instance;
import com.nebula.nebulaCloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Instance entity operations.
 */
@Repository
public interface InstanceRepository extends JpaRepository<Instance, Long> {

    /**
     * Find all instances belonging to a specific user.
     */
    List<Instance> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find instance by ID and user (for security).
     */
    Optional<Instance> findByIdAndUser(Long id, User user);

    /**
     * Check if an instance name already exists for a user.
     */
    boolean existsByNameAndUser(String name, User user);

    /**
     * Find instances by status.
     */
    List<Instance> findByStatus(Instance.Status status);
    /**
     * Find instances by container ID.
     */
    List<Instance> findByContainerId(Long containerId);

    /**
     * Get instance with all related entities loaded.
     */
    @Query("SELECT i FROM Instance i " +
           "LEFT JOIN FETCH i.container c " +
           "LEFT JOIN FETCH c.engine " +
           "LEFT JOIN FETCH i.userDb " +
           "LEFT JOIN FETCH i.user " +
           "WHERE i.id = :id")
    Optional<Instance> findByIdWithDetails(@Param("id") Long id);
}

