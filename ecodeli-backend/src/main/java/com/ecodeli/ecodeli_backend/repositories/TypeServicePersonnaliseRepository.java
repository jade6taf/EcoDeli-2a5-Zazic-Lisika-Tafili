package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.TypeServicePersonnalise;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeServicePersonnaliseRepository extends JpaRepository<TypeServicePersonnalise, Long> {

    List<TypeServicePersonnalise> findByCategorieParenteOrderByNomServiceAsc(ServiceType categorieParente);

    List<TypeServicePersonnalise> findByValideParAdminTrueOrderByNomServiceAsc();

    List<TypeServicePersonnalise> findByActifTrueOrderByNomServiceAsc();

    List<TypeServicePersonnalise> findByValideParAdminTrueAndActifTrueOrderByNomServiceAsc();

    List<TypeServicePersonnalise> findByCategorieParenteAndValideParAdminTrueAndActifTrueOrderByNomServiceAsc(
        ServiceType categorieParente);

    List<TypeServicePersonnalise> findByCreateurOrderByDateCreationDesc(Utilisateur createur);

    List<TypeServicePersonnalise> findByValideParAdminFalseOrderByDateCreationDesc();

    boolean existsByNomServiceAndCategorieParente(String nomService, ServiceType categorieParente);

    @Query("SELECT t FROM TypeServicePersonnalise t WHERE " +
           "LOWER(t.nomService) LIKE LOWER(CONCAT('%', :nomService, '%')) " +
           "AND t.valideParAdmin = true AND t.actif = true " +
           "ORDER BY t.nomService ASC")
    List<TypeServicePersonnalise> findByNomServiceContainingIgnoreCase(@Param("nomService") String nomService);

    Long countByCategorieParente(ServiceType categorieParente);

    Long countByValideParAdminFalse();
}
