package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Annonce.StatutAnnonce;
import com.ecodeli.ecodeli_backend.models.Annonce.TypeAnnonce;
import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.models.Livreur;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.repositories.AnnonceRepository;
import com.ecodeli.ecodeli_backend.repositories.LivraisonRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LivraisonRepository livraisonRepository;


    public AnnonceService(AnnonceRepository annonceRepository,
        UtilisateurRepository utilisateurRepository, LivraisonRepository livraisonRepository) {
        this.annonceRepository = annonceRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.livraisonRepository = livraisonRepository;

    }

    public List<Annonce> getAllAnnonces() {
        return annonceRepository.findAll();
    }

    public Optional<Annonce> getAnnonceById(Integer id) {
        return annonceRepository.findById(id);
    }

    public List<Annonce> getAnnoncesByType(TypeAnnonce typeAnnonce) {
        return annonceRepository.findByTypeAnnonce(typeAnnonce);
    }

    public List<Annonce> getAnnoncesByStatut(StatutAnnonce statut) {
        return annonceRepository.findByStatut(statut);
    }

    public List<Annonce> getAnnoncesByExpediteur(Integer idExpediteur) {
        return annonceRepository.findByExpediteurIdUtilisateur(idExpediteur);
    }

    public List<Annonce> searchAnnoncesByDepartureCity(String ville) {
        return annonceRepository.findActiveAnnoncesByDepartureCity(ville);
    }

    public List<Annonce> searchAnnoncesByArrivalCity(String ville) {
        return annonceRepository.findActiveAnnoncesByArrivalCity(ville);
    }

    public List<Annonce> searchAnnoncesByPriceRange(BigDecimal min, BigDecimal max) {
        return annonceRepository.findActiveAnnoncesByPriceRange(min, max);
    }

    @Transactional
    public Annonce createAnnonce(Annonce annonce, Integer idExpediteur) {

        Utilisateur expediteur = utilisateurRepository.findById(idExpediteur)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur 'Expéditeur' non trouvé avec l'ID: " + idExpediteur));

        if (annonce.getDateDebut() != null && annonce.getDateFin() != null) {
            if (annonce.getDateDebut().isAfter(annonce.getDateFin())) {
                throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
            }
            if (annonce.getDateDebut().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("La date de début ne peut pas être dans le passé");
            }
        }
        annonce.setExpediteur(expediteur);
        if (annonce.getStatut() == null)
            annonce.setStatut(StatutAnnonce.PUBLIEE);
        if (annonce.getTypeAnnonce() == null)
            annonce.setTypeAnnonce(TypeAnnonce.unique);
        return annonceRepository.save(annonce);
    }

    @Transactional
    public Annonce updateAnnonce(Integer id, Annonce annonceDetails) {

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'ID: " + id));

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE)
            throw new IllegalArgumentException("Seule une annonce publiée peut être modifiée");

        if (annonceDetails.getDateDebut() != null && annonceDetails.getDateFin() != null) {
            if (annonceDetails.getDateDebut().isAfter(annonceDetails.getDateFin())) {
                throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
            }
            LocalDateTime now = LocalDateTime.now();
            if (annonceDetails.getDateDebut().isBefore(now) && annonce.getDateDebut().isAfter(now)) {
                throw new IllegalArgumentException("La date de début ne peut pas être modifiée pour être dans le passé");
            }
            annonce.setDateDebut(annonceDetails.getDateDebut());
            annonce.setDateFin(annonceDetails.getDateFin());
        } else if (annonceDetails.getDateDebut() != null) {
            if (annonceDetails.getDateDebut().isAfter(annonce.getDateFin())) {
                throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
            }
            if (annonceDetails.getDateDebut().isBefore(LocalDateTime.now()) && annonce.getDateDebut().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("La date de début ne peut pas être modifiée pour être dans le passé");
            }
            annonce.setDateDebut(annonceDetails.getDateDebut());
        } else if (annonceDetails.getDateFin() != null) {
            if (annonce.getDateDebut().isAfter(annonceDetails.getDateFin())) {
                throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début");
            }
            annonce.setDateFin(annonceDetails.getDateFin());
        }

        if (annonceDetails.getTitre() != null) {
            annonce.setTitre(annonceDetails.getTitre());
        }
        if (annonceDetails.getDescription() != null) {
            annonce.setDescription(annonceDetails.getDescription());
        }
        if (annonceDetails.getPrixUnitaire() != null) {
            annonce.setPrixUnitaire(annonceDetails.getPrixUnitaire());
        }
        if (annonceDetails.getTypeAnnonce() != null) {
            annonce.setTypeAnnonce(annonceDetails.getTypeAnnonce());
        }
        if (annonceDetails.getAdresseDepart() != null) {
            annonce.setAdresseDepart(annonceDetails.getAdresseDepart());
        }
        if (annonceDetails.getAdresseFin() != null) {
            annonce.setAdresseFin(annonceDetails.getAdresseFin());
        }
        return annonceRepository.save(annonce);
    }

    @Transactional
    public Annonce cancelAnnonce(Integer id) {

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'ID: " + id));

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE)
            throw new IllegalArgumentException("Seule une annonce publiée");

        annonce.setStatut(StatutAnnonce.ANNULEE);
        return annonceRepository.save(annonce);
    }

    @Transactional
    public void deleteAnnonce(Integer id) {

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'ID: " + id));

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE)
            throw new IllegalArgumentException("Seule une annonce publiée peut être supprimée");

        annonceRepository.deleteById(id);
    }

    @Transactional
    public Annonce demanderValidation(Integer id, Integer idLivreur) {

        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'ID: " + id));
        Utilisateur utilisateur = utilisateurRepository.findById(idLivreur)
                .orElseThrow(() -> new IllegalArgumentException("Livreur non trouvé avec l'ID: " + idLivreur));

        if (!(utilisateur instanceof Livreur)) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un livreur");
        }
        Livreur livreur = (Livreur) utilisateur;
        if (annonce.getStatut() != StatutAnnonce.PUBLIEE) {
            throw new IllegalArgumentException("Cette annonce n'est pas disponible pour une demande de validation");
        }
        annonce.setLivreur(livreur);
        annonce.setStatut(StatutAnnonce.VALIDEE);
        Annonce savedAnnonce = annonceRepository.save(annonce);

        try {
            Livraison livraison = new Livraison();
            livraison.setAnnonce(savedAnnonce);
            livraison.setStatut(Livraison.StatutLivraison.VALIDEE);
            livraison.setDateDebut(savedAnnonce.getDateDebut());
            livraison.setDateFin(savedAnnonce.getDateFin());
            livraison.setAdresseEnvoi(savedAnnonce.getAdresseDepart());
            livraison.setAdresseDeLivraison(savedAnnonce.getAdresseFin());
            livraison.setExpediteur(savedAnnonce.getExpediteur());
            if (livraison.getCodePostalEnvoi() == null || livraison.getCodePostalEnvoi().isEmpty()) {
                livraison.setCodePostalEnvoi("00000");
            }
            if (livraison.getCodePostalLivraison() == null || livraison.getCodePostalLivraison().isEmpty()) {
                livraison.setCodePostalLivraison("00000");
            }
            if (livraison.getDestinataire() == null) {
                livraison.setDestinataire(savedAnnonce.getExpediteur());
            }
            livraisonRepository.save(livraison);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la livraison: " + e.getMessage());
        }
        return savedAnnonce;
    }
}
