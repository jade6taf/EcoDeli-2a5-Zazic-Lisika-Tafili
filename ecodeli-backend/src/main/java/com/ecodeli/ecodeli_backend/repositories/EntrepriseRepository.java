package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Integer> {

    Optional<Entreprise> findBySiret(String siret);

    Optional<Entreprise> findByEmail(String email);

    boolean existsBySiret(String siret);

    boolean existsByEmail(String email);

    // List<Entreprise> findByStatutJuridique(StatutJuridique statutJuridique);

    List<Entreprise> findBySecteurActivite(String secteurActivite);

    List<Entreprise> findByVille(String ville);

    List<Entreprise> findByValidationParAd(Boolean validationParAd);
}