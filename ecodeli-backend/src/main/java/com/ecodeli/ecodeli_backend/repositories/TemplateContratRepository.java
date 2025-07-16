package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.TemplateContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateContratRepository extends JpaRepository<TemplateContrat, Integer> {
    
    List<TemplateContrat> findByActifTrueOrderByDateCreationDesc();
    
    List<TemplateContrat> findAllByOrderByDateCreationDesc();
    
    Optional<TemplateContrat> findByNomTemplate(String nomTemplate);
    
    boolean existsByNomTemplate(String nomTemplate);
}