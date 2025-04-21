package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.models.Livraison.StatutLivraison;
import com.ecodeli.ecodeli_backend.repositories.AnnonceRepository;
import com.ecodeli.ecodeli_backend.repositories.LivraisonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LivraisonService {

    private final LivraisonRepository livraisonRepository;
    private final AnnonceRepository annonceRepository;

    public LivraisonService(LivraisonRepository livraisonRepository, AnnonceRepository annonceRepository) {
        this.livraisonRepository = livraisonRepository;
        this.annonceRepository = annonceRepository;
    }

    public List<Livraison> getLivraisonsByLivreur(Integer idLivreur) {
        return livraisonRepository.findByAnnonce_Livreur_IdUtilisateur(idLivreur);
    }

    @Transactional
    public Livraison demarrerLivraison(Integer id) {

        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + id));

        if (livraison.getStatut() != StatutLivraison.VALIDEE) {
            throw new IllegalArgumentException("Seule une livraison validée peut être démarrée");
        }
        livraison.setStatut(StatutLivraison.EN_COURS);
        Annonce annonce = livraison.getAnnonce();
        if (annonce != null) {
            annonce.setStatut(Annonce.StatutAnnonce.EN_COURS);
            annonceRepository.save(annonce);
        }
        return livraisonRepository.save(livraison);
    }

    @Transactional
    public Livraison terminerLivraison(Integer id) {

        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + id));

        if (livraison.getStatut() != StatutLivraison.EN_COURS) {
            throw new IllegalArgumentException("Seule une livraison en cours peut être terminée");
        }
        livraison.setStatut(StatutLivraison.TERMINEE);
        Annonce annonce = livraison.getAnnonce();
        if (annonce != null) {
            annonce.setStatut(Annonce.StatutAnnonce.TERMINEE);
            annonceRepository.save(annonce);
        }
        return livraisonRepository.save(livraison);
    }

    @Transactional
    public Livraison annulerLivraison(Integer id) {

        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée avec l'ID: " + id));

        if (livraison.getStatut() == StatutLivraison.TERMINEE) {
            throw new IllegalArgumentException("Une livraison terminée ne peut pas être annulée");
        }
        livraison.setStatut(StatutLivraison.ANNULEE);
        Annonce annonce = livraison.getAnnonce();
        if (annonce != null) {
            annonce.setStatut(Annonce.StatutAnnonce.ANNULEE);
            annonceRepository.save(annonce);
        }
        return livraisonRepository.save(livraison);
    }
}
