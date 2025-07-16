package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.ContratCommercant;
import com.ecodeli.ecodeli_backend.models.Commercant;
import com.ecodeli.ecodeli_backend.models.ContratCommercant.StatutContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratCommercantRepository extends JpaRepository<ContratCommercant, Integer> {
    
    Optional<ContratCommercant> findByCommercant(Commercant commercant);
    
    Optional<ContratCommercant> findByCommercant_IdUtilisateur(Integer idCommercant);
    
    List<ContratCommercant> findByStatut(StatutContrat statut);
    
    List<ContratCommercant> findByStatutOrderByDateDemandeDesc(StatutContrat statut);
    
    List<ContratCommercant> findAllByOrderByDateDemandeDesc();
    
    @Query("SELECT COUNT(c) FROM ContratCommercant c WHERE c.statut = :statut")
    Long countByStatut(StatutContrat statut);
    
    @Query("SELECT c FROM ContratCommercant c WHERE c.statut IN :statuts ORDER BY c.dateDemande DESC")
    List<ContratCommercant> findByStatutInOrderByDateDemandeDesc(List<StatutContrat> statuts);
    
    boolean existsByCommercant_IdUtilisateur(Integer idCommercant);
}