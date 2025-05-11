package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Justificatif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JustificatifRepository extends JpaRepository<Justificatif, Integer> {
    List<Justificatif> findByValidationParAdFalseAndCommentaireIsNull();
    List<Justificatif> findByValidationParAdTrue();
    List<Justificatif> findByValidationParAdFalseAndCommentaireIsNotNull();
    List<Justificatif> findByUtilisateur_IdUtilisateur(Integer userId);
}
