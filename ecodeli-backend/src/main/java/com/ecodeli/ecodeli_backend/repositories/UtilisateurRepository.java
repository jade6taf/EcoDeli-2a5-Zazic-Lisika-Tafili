package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByEmail(String email);

    @Query("SELECT u FROM Utilisateur u WHERE TYPE(u) = CASE " +
           "WHEN :type = 'CLIENT' THEN Client " +
           "WHEN :type = 'LIVREUR' THEN Livreur " +
           "WHEN :type = 'COMMERCANT' THEN Commercant " +
           "WHEN :type = 'PRESTATAIRE' THEN Prestataire " +
           "END")
    List<Utilisateur> findByType(@Param("type") String type);

    boolean existsByEmail(String email);
}
