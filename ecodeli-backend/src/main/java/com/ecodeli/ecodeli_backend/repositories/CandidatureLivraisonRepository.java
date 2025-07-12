package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.CandidatureLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatureLivraisonRepository extends JpaRepository<CandidatureLivraison, Long> {

    List<CandidatureLivraison> findByAnnonceIdAnnonce(Integer annonceId);

    List<CandidatureLivraison> findByLivreurIdUtilisateur(Integer livreurId);

    boolean existsByAnnonceIdAnnonceAndLivreurIdUtilisateur(Integer annonceId, Integer livreurId);
}
