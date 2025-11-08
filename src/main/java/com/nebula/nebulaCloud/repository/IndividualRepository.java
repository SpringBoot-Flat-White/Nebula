package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Individual;
import com.nebula.nebulaCloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndividualRepository extends JpaRepository<Individual, Long> {
}
