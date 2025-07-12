package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Retrait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RetraitRepository extends JpaRepository<Retrait, Integer> {
    List<Retrait> findByLivreurIdUtilisateurOrderByDateDemandeDesc(Integer livreurId);

    List<Retrait> findByStatutOrderByDateDemandeDesc(Retrait.StatutRetrait statut);
}
