package com.ecodeli.ecodeli_backend.repositories;

import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.models.Livraison.StatutLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivraisonRepository extends JpaRepository<Livraison, Integer> {
    List<Livraison> findByAnnonce_Livreur_IdUtilisateur(Integer idLivreur);

    List<Livraison> findByStatut(StatutLivraison statut);
    List<Livraison> findByStatutAndEntrepotVille(StatutLivraison statut, String entrepotVille);
}
