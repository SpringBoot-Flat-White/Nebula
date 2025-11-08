package com.nebula.nebulaCloud.repository;

import com.nebula.nebulaCloud.model.Individual;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndividualRepository extends JpaRepository<Individual,Long> {
}
