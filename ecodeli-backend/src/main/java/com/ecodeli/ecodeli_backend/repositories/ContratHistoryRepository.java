package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.ContratHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratHistoryRepository extends JpaRepository<ContratHistory, Integer> {

    List<ContratHistory> findByContratIdContratOrderByDateActionDesc(Integer idContrat);

    List<ContratHistory> findByUtilisateurActionIdUtilisateurOrderByDateActionDesc(Integer idUtilisateur);

    List<ContratHistory> findTop10ByOrderByDateActionDesc();
}
