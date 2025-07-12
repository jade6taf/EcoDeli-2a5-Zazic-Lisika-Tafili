package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.DemandeService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeServiceRepository extends JpaRepository<DemandeService, Long> {}
