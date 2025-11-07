package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    boolean existsByName(String name);
}








