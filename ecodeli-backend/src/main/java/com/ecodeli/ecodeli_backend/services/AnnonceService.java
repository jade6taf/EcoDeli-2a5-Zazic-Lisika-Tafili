package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Annonce.StatutAnnonce;
import com.ecodeli.ecodeli_backend.models.Annonce.TypeAnnonce;
import com.ecodeli.ecodeli_backend.models.Colis;
import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.models.Livreur;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.repositories.AnnonceRepository;
import com.ecodeli.ecodeli_backend.repositories.ColisRepository;
import com.ecodeli.ecodeli_backend.repositories.LivraisonRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import com.ecodeli.ecodeli_backend.models.Coordinates; // Ajout
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
    private final ColisRepository colisRepository;
    private final GeocodingService geocodingService;

    public AnnonceService(AnnonceRepository annonceRepository,
                         UtilisateurRepository utilisateurRepository,
                         LivraisonRepository livraisonRepository,
                         ColisRepository colisRepository,
                         GeocodingService geocodingService) {
        this.annonceRepository = annonceRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.livraisonRepository = livraisonRepository;
        this.colisRepository = colisRepository;
        this.geocodingService = geocodingService;
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

        if (annonce.getColis() != null) {
            Colis colis = annonce.getColis();
            colis = colisRepository.save(colis);
            annonce.setColis(colis);
        }

        if (annonce.getDestinataire() != null) {
            Utilisateur destinataire = annonce.getDestinataire();
            Optional<Utilisateur> existingUser = utilisateurRepository.findByEmail(destinataire.getEmail());
            if (existingUser.isPresent()) {
                annonce.setDestinataire(existingUser.get());
            } else {
                throw new IllegalArgumentException("Destinataire non trouvé avec l'email: " + destinataire.getEmail() + ". Le destinataire doit être un utilisateur existant.");
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

        if (annonceDetails.getColis() != null) {
            Colis existingColis = annonce.getColis();
            Colis updatedColis = annonceDetails.getColis();

            if (existingColis != null) {
                if (updatedColis.getPoids() != null)
                    existingColis.setPoids(updatedColis.getPoids());
                if (updatedColis.getLongueur() != null)
                    existingColis.setLongueur(updatedColis.getLongueur());
                if (updatedColis.getLargeur() != null)
                    existingColis.setLargeur(updatedColis.getLargeur());
                if (updatedColis.getHauteur() != null)
                    existingColis.setHauteur(updatedColis.getHauteur());
                if (updatedColis.getFragile() != null)
                    existingColis.setFragile(updatedColis.getFragile());
                if (updatedColis.getDescription() != null)
                    existingColis.setDescription(updatedColis.getDescription());
                colisRepository.save(existingColis);
            } else {
                updatedColis = colisRepository.save(updatedColis);
                annonce.setColis(updatedColis);
            }
        }

        if (annonceDetails.getDestinataire() != null) {
            Utilisateur existingDestinataire = annonce.getDestinataire();
            Utilisateur updatedDestinataire = annonceDetails.getDestinataire();
            if (existingDestinataire != null) {
                if (updatedDestinataire.getNom() != null)
                    existingDestinataire.setNom(updatedDestinataire.getNom());
                if (updatedDestinataire.getPrenom() != null)
                    existingDestinataire.setPrenom(updatedDestinataire.getPrenom());
                if (updatedDestinataire.getEmail() != null)
                    existingDestinataire.setEmail(updatedDestinataire.getEmail());
                if (updatedDestinataire.getTelephone() != null)
                    existingDestinataire.setTelephone(updatedDestinataire.getTelephone());
                utilisateurRepository.save(existingDestinataire);
            } else {
                Optional<Utilisateur> existingUser = Optional.empty();
                if (updatedDestinataire.getEmail() != null) {
                    existingUser = utilisateurRepository.findByEmail(updatedDestinataire.getEmail());
                }
                if (existingUser.isPresent()) {
                    annonce.setDestinataire(existingUser.get());
                } else {
                    updatedDestinataire = utilisateurRepository.save(updatedDestinataire);
                    annonce.setDestinataire(updatedDestinataire);
                }
            }
        }
        return annonceRepository.save(annonce);
    }

    @Transactional
    public Annonce cancelAnnonce(Integer id) {
        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'ID: " + id));

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE)
            throw new IllegalArgumentException("Seule une annonce publiée peut être annulée");

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
            livraison.setDestinataire(savedAnnonce.getDestinataire());
            livraison.setColis(savedAnnonce.getColis());

            if (savedAnnonce.getPrixUnitaire() != null) {
                livraison.setPrix(savedAnnonce.getPrixUnitaire().intValue());
            }

            // Géocodage de l'adresse de départ
            Optional<Coordinates> departCoordsOpt = geocodingService.geocodeAddress(
                savedAnnonce.getAdresseDepart(),
                null,
                null
            ).block();

            if (departCoordsOpt.isPresent()) {
                livraison.setLatitudeEnvoi(departCoordsOpt.get().getLatitude());
                livraison.setLongitudeEnvoi(departCoordsOpt.get().getLongitude());
            } else {
                throw new IllegalArgumentException("Adresse de départ invalide ou non géocodable: " + savedAnnonce.getAdresseDepart());
            }

            // Géocodage de l'adresse d'arrivée
            Optional<Coordinates> arriveeCoordsOpt = geocodingService.geocodeAddress(
                savedAnnonce.getAdresseFin(),
                null,
                null
            ).block();

            if (arriveeCoordsOpt.isPresent()) {
                livraison.setLatitudeLivraison(arriveeCoordsOpt.get().getLatitude());
                livraison.setLongitudeLivraison(arriveeCoordsOpt.get().getLongitude());
            } else {
                throw new IllegalArgumentException("Adresse d'arrivée invalide ou non géocodable: " + savedAnnonce.getAdresseFin());
            }
            if (livraison.getCodePostalEnvoi() == null || livraison.getCodePostalEnvoi().isEmpty()) {
                livraison.setCodePostalEnvoi("00000");
            }
            if (livraison.getCodePostalLivraison() == null || livraison.getCodePostalLivraison().isEmpty()) {
                livraison.setCodePostalLivraison("00000");
            }
            livraisonRepository.save(livraison);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de validation d'adresse lors de la création de la livraison: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la livraison: " + e.getMessage());
        }
        return savedAnnonce;
    }
}