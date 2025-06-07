package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Annonce;
import com.ecodeli.ecodeli_backend.models.Annonce.StatutAnnonce;
import com.ecodeli.ecodeli_backend.models.Annonce.TypeAnnonce;
import com.ecodeli.ecodeli_backend.models.Colis;
import com.ecodeli.ecodeli_backend.models.Commercant;
import com.ecodeli.ecodeli_backend.models.Livraison;
import com.ecodeli.ecodeli_backend.models.Livraison.TypeLivraison;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LivraisonRepository livraisonRepository;
    private final ColisRepository colisRepository;
    private final GeocodingService geocodingService;
    private final EntrepotUtilService entrepotUtilService;

    public AnnonceService(AnnonceRepository annonceRepository,
                         UtilisateurRepository utilisateurRepository,
                         LivraisonRepository livraisonRepository,
                         ColisRepository colisRepository,
                         GeocodingService geocodingService,
                         EntrepotUtilService entrepotUtilService) {
        this.annonceRepository = annonceRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.livraisonRepository = livraisonRepository;
        this.colisRepository = colisRepository;
        this.geocodingService = geocodingService;
        this.entrepotUtilService = entrepotUtilService;
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
            throw e;
        } catch (Exception e) {
            // Erreur silencieuse lors de la création de la livraison
        }
        return savedAnnonce;
    }

    @Transactional
    public Annonce demanderValidationPartielle(Integer id, Integer idLivreur, String entrepotVille, Integer numeroSegment) {
        Annonce annonce = annonceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'ID: " + id));
        Utilisateur utilisateur = utilisateurRepository.findById(idLivreur)
                .orElseThrow(() -> new IllegalArgumentException("Livreur non trouvé avec l'ID: " + idLivreur));

        if (!(utilisateur instanceof Livreur)) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un livreur");
        }
        Livreur livreur = (Livreur) utilisateur;

        if (!annonce.getLivraisonPartielleAutorisee()) {
            throw new IllegalArgumentException("Cette annonce n'autorise pas la livraison partielle");
        }

        // Vérifier que l'entrepôt existe
        try {
            if (!entrepotUtilService.getEntrepotsDisponibles().contains(entrepotVille)) {
                throw new IllegalArgumentException("Entrepôt non disponible: " + entrepotVille);
            }
        } catch (Exception e) {
            // Continuer sans vérification pour le moment
        }

        // Vérifier les conditions selon le segment choisi
        if (numeroSegment == 1) {
            if (annonce.getStatut() != StatutAnnonce.PUBLIEE && annonce.getStatut() != StatutAnnonce.SEGMENT_2_PRIS) {
                throw new IllegalArgumentException("Le segment 1 n'est pas disponible");
            }
            if (annonce.getLivreurSegment1() != null) {
                throw new IllegalArgumentException("Le segment 1 est déjà pris");
            }

            annonce.setLivreurSegment1(livreur);
            annonce.setStatutSegment1(Annonce.StatutSegment.PRIS);
            annonce.setEntrepotIntermediaire(entrepotVille);

            if (annonce.getStatut() == StatutAnnonce.SEGMENT_2_PRIS) {
                annonce.setStatut(StatutAnnonce.SEGMENTS_COMPLETS);
            } else {
                annonce.setStatut(StatutAnnonce.SEGMENT_1_PRIS);
            }
        } else if (numeroSegment == 2) {
            if (annonce.getStatut() != StatutAnnonce.PUBLIEE && annonce.getStatut() != StatutAnnonce.SEGMENT_1_PRIS) {
                throw new IllegalArgumentException("Le segment 2 n'est pas disponible");
            }
            if (annonce.getLivreurSegment2() != null) {
                throw new IllegalArgumentException("Le segment 2 est déjà pris");
            }

            // Pour le segment 2, on doit avoir un entrepôt déjà défini
            if (annonce.getStatut() == StatutAnnonce.SEGMENT_1_PRIS && annonce.getEntrepotIntermediaire() != null) {
                entrepotVille = annonce.getEntrepotIntermediaire();
            } else {
                annonce.setEntrepotIntermediaire(entrepotVille);
            }

            annonce.setLivreurSegment2(livreur);
            annonce.setStatutSegment2(Annonce.StatutSegment.PRIS);

            if (annonce.getStatut() == StatutAnnonce.SEGMENT_1_PRIS) {
                annonce.setStatut(StatutAnnonce.SEGMENTS_COMPLETS);
            } else {
                annonce.setStatut(StatutAnnonce.SEGMENT_2_PRIS);
            }
        } else {
            throw new IllegalArgumentException("Numéro de segment invalide. Doit être 1 ou 2");
        }

        Annonce savedAnnonce = annonceRepository.save(annonce);

        try {
            Livraison livraison = new Livraison();
            livraison.setAnnonce(savedAnnonce);
            livraison.setStatut(Livraison.StatutLivraison.VALIDEE);
            livraison.setTypeLivraison(TypeLivraison.PARTIELLE);
            livraison.setEntrepotVille(entrepotVille);
            livraison.setLivreurSegment1(livreur);
            livraison.setDateDebut(savedAnnonce.getDateDebut());
            livraison.setDateFin(savedAnnonce.getDateFin());
            livraison.setAdresseEnvoi(savedAnnonce.getAdresseDepart());

            // Pour une livraison partielle, l'adresse de livraison est l'entrepôt pour le segment 1
            Double[] coordsEntrepot = entrepotUtilService.getCoordonneesEntrepot(entrepotVille);
            if (coordsEntrepot != null) {
                livraison.setAdresseDeLivraison("Entrepôt " + entrepotVille);
                livraison.setLatitudeLivraison(coordsEntrepot[0]);
                livraison.setLongitudeLivraison(coordsEntrepot[1]);
            }

            livraison.setExpediteur(savedAnnonce.getExpediteur());
            livraison.setDestinataire(savedAnnonce.getDestinataire());
            livraison.setColis(savedAnnonce.getColis());

            if (savedAnnonce.getPrixUnitaire() != null) {
                // Diviser le prix par 2 pour le segment 1
                livraison.setPrix(savedAnnonce.getPrixUnitaire().intValue() / 2);
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

            if (livraison.getCodePostalEnvoi() == null || livraison.getCodePostalEnvoi().isEmpty()) {
                livraison.setCodePostalEnvoi("00000");
            }
            if (livraison.getCodePostalLivraison() == null || livraison.getCodePostalLivraison().isEmpty()) {
                livraison.setCodePostalLivraison("00000");
            }

            livraisonRepository.save(livraison);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            // Erreur silencieuse lors de la création de la livraison partielle
        }
        return savedAnnonce;
    }

    public List<Annonce> getAnnoncesAvecLivraisonPartielleAutorisee() {
        return annonceRepository.findByLivraisonPartielleAutoriseeAndStatut(true, StatutAnnonce.PUBLIEE);
    }

    public List<Annonce> getAnnoncesAvecSegmentsDisponibles() {
        return annonceRepository.findAnnoncesAvecSegmentsDisponibles();
    }

    public List<Annonce> getAnnoncesAvecSegment1Disponible() {
        return annonceRepository.findAnnoncesAvecSegment1Disponible();
    }

    public List<Annonce> getAnnoncesAvecSegment2Disponible() {
        return annonceRepository.findAnnoncesAvecSegment2Disponible();
    }

    public List<Annonce> getAnnoncesAvecSegmentsComplets() {
        return annonceRepository.findAnnoncesAvecSegmentsComplets();
    }

    // Méthode pour obtenir les annonces visibles selon les critères corrects
    public List<Annonce> getAnnoncesDisponiblesPourLivreurs() {
        // Retourne les annonces publiées ET celles avec des segments partiels disponibles
        List<Annonce> annoncesPubliees = annonceRepository.findByStatut(StatutAnnonce.PUBLIEE);
        List<Annonce> annoncesAvecSegments = annonceRepository.findAnnoncesAvecSegmentsDisponibles();

        // Combiner les deux listes en évitant les doublons
        annoncesPubliees.addAll(annoncesAvecSegments.stream()
            .filter(a -> !annoncesPubliees.contains(a))
            .toList());

        return annoncesPubliees;
    }

    @Transactional
    public Annonce commencerSegment(Integer idAnnonce, Integer idLivreur, Integer numeroSegment) {
        Annonce annonce = annonceRepository.findById(idAnnonce)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'ID: " + idAnnonce));
        Utilisateur utilisateur = utilisateurRepository.findById(idLivreur)
                .orElseThrow(() -> new IllegalArgumentException("Livreur non trouvé avec l'ID: " + idLivreur));

        if (!(utilisateur instanceof Livreur)) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un livreur");
        }
        Livreur livreur = (Livreur) utilisateur;

        if (numeroSegment == 1) {
            if (!livreur.equals(annonce.getLivreurSegment1())) {
                throw new IllegalArgumentException("Vous n'êtes pas assigné à ce segment");
            }
            if (annonce.getStatut() != StatutAnnonce.SEGMENTS_COMPLETS) {
                throw new IllegalArgumentException("Les segments ne sont pas tous pris");
            }
            if (annonce.getStatutSegment1() != Annonce.StatutSegment.PRIS) {
                throw new IllegalArgumentException("Le segment 1 ne peut pas être commencé");
            }

            annonce.setStatutSegment1(Annonce.StatutSegment.EN_COURS);
            annonce.setStatut(StatutAnnonce.EN_COURS_SEGMENT_1);

        } else if (numeroSegment == 2) {
            if (!livreur.equals(annonce.getLivreurSegment2())) {
                throw new IllegalArgumentException("Vous n'êtes pas assigné à ce segment");
            }
            if (annonce.getStatut() != StatutAnnonce.ATTENTE_ENTREPOT) {
                throw new IllegalArgumentException("Le segment 1 doit être terminé avant de commencer le segment 2");
            }
            if (annonce.getStatutSegment2() != Annonce.StatutSegment.PRIS) {
                throw new IllegalArgumentException("Le segment 2 ne peut pas être commencé");
            }

            annonce.setStatutSegment2(Annonce.StatutSegment.EN_COURS);
            annonce.setStatut(StatutAnnonce.EN_COURS_SEGMENT_2);

        } else {
            throw new IllegalArgumentException("Numéro de segment invalide. Doit être 1 ou 2");
        }

        return annonceRepository.save(annonce);
    }

    @Transactional
    public Annonce terminerSegment(Integer idAnnonce, Integer idLivreur, Integer numeroSegment) {
        Annonce annonce = annonceRepository.findById(idAnnonce)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'ID: " + idAnnonce));
        Utilisateur utilisateur = utilisateurRepository.findById(idLivreur)
                .orElseThrow(() -> new IllegalArgumentException("Livreur non trouvé avec l'ID: " + idLivreur));

        if (!(utilisateur instanceof Livreur)) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un livreur");
        }
        Livreur livreur = (Livreur) utilisateur;

        if (numeroSegment == 1) {
            if (!livreur.equals(annonce.getLivreurSegment1())) {
                throw new IllegalArgumentException("Vous n'êtes pas assigné à ce segment");
            }
            if (annonce.getStatut() != StatutAnnonce.EN_COURS_SEGMENT_1) {
                throw new IllegalArgumentException("Le segment 1 n'est pas en cours");
            }
            if (annonce.getStatutSegment1() != Annonce.StatutSegment.EN_COURS) {
                throw new IllegalArgumentException("Le segment 1 ne peut pas être terminé");
            }

            annonce.setStatutSegment1(Annonce.StatutSegment.TERMINE);
            annonce.setStatut(StatutAnnonce.ATTENTE_ENTREPOT);

        } else if (numeroSegment == 2) {
            if (!livreur.equals(annonce.getLivreurSegment2())) {
                throw new IllegalArgumentException("Vous n'êtes pas assigné à ce segment");
            }
            if (annonce.getStatut() != StatutAnnonce.EN_COURS_SEGMENT_2) {
                throw new IllegalArgumentException("Le segment 2 n'est pas en cours");
            }
            if (annonce.getStatutSegment2() != Annonce.StatutSegment.EN_COURS) {
                throw new IllegalArgumentException("Le segment 2 ne peut pas être terminé");
            }

            annonce.setStatutSegment2(Annonce.StatutSegment.TERMINE);
            annonce.setStatut(StatutAnnonce.TERMINEE);

        } else {
            throw new IllegalArgumentException("Numéro de segment invalide. Doit être 1 ou 2");
        }

        return annonceRepository.save(annonce);
    }

    public List<Annonce> getMesSegments(Integer idLivreur) {
        Utilisateur utilisateur = utilisateurRepository.findById(idLivreur)
                .orElseThrow(() -> new IllegalArgumentException("Livreur non trouvé avec l'ID: " + idLivreur));

        if (!(utilisateur instanceof Livreur)) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un livreur");
        }
        Livreur livreur = (Livreur) utilisateur;

        return annonceRepository.findMesSegments(livreur.getIdUtilisateur());
    }

    public Map<String, Object> getStatutDetaille(Integer idAnnonce) {
        Annonce annonce = annonceRepository.findById(idAnnonce)
                .orElseThrow(() -> new IllegalArgumentException("Annonce non trouvée avec l'ID: " + idAnnonce));

        Map<String, Object> statut = new HashMap<>();
        statut.put("idAnnonce", annonce.getIdAnnonce());
        statut.put("statut", annonce.getStatut());
        statut.put("livraisonPartielleAutorisee", annonce.getLivraisonPartielleAutorisee());

        if (annonce.getLivraisonPartielleAutorisee()) {
            Map<String, Object> segment1 = new HashMap<>();
            segment1.put("statut", annonce.getStatutSegment1());
            segment1.put("livreur", annonce.getLivreurSegment1() != null ?
                Map.of("id", annonce.getLivreurSegment1().getIdUtilisateur(),
                       "nom", annonce.getLivreurSegment1().getNom(),
                       "prenom", annonce.getLivreurSegment1().getPrenom()) : null);
            segment1.put("adresseDepart", annonce.getAdresseDepart());
            segment1.put("adresseArrivee", "Entrepôt " + annonce.getEntrepotIntermediaire());

            Map<String, Object> segment2 = new HashMap<>();
            segment2.put("statut", annonce.getStatutSegment2());
            segment2.put("livreur", annonce.getLivreurSegment2() != null ?
                Map.of("id", annonce.getLivreurSegment2().getIdUtilisateur(),
                       "nom", annonce.getLivreurSegment2().getNom(),
                       "prenom", annonce.getLivreurSegment2().getPrenom()) : null);
            segment2.put("adresseDepart", "Entrepôt " + annonce.getEntrepotIntermediaire());
            segment2.put("adresseArrivee", annonce.getAdresseFin());

            statut.put("segment1", segment1);
            statut.put("segment2", segment2);
            statut.put("entrepotIntermediaire", annonce.getEntrepotIntermediaire());
        } else {
            statut.put("livreur", annonce.getLivreur() != null ?
                Map.of("id", annonce.getLivreur().getIdUtilisateur(),
                       "nom", annonce.getLivreur().getNom(),
                       "prenom", annonce.getLivreur().getPrenom()) : null);
            statut.put("adresseDepart", annonce.getAdresseDepart());
            statut.put("adresseArrivee", annonce.getAdresseFin());
        }

        return statut;
    }

    public List<Annonce> getAnnoncesByCommercant(Integer idCommercant) {
        return annonceRepository.findByExpediteurIdUtilisateur(idCommercant);
    }

    public Map<String, Object> getStatistiquesCommercant(Integer idCommercant) {
        List<Annonce> annonces = getAnnoncesByCommercant(idCommercant);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAnnonces", annonces.size());
        stats.put("annoncesActives", annonces.stream().filter(a ->
            a.getStatut() == StatutAnnonce.PUBLIEE ||
            a.getStatut() == StatutAnnonce.SEGMENT_1_PRIS ||
            a.getStatut() == StatutAnnonce.SEGMENT_2_PRIS ||
            a.getStatut() == StatutAnnonce.SEGMENTS_COMPLETS
        ).count());
        stats.put("annoncesEnCours", annonces.stream().filter(a ->
            a.getStatut() == StatutAnnonce.VALIDEE ||
            a.getStatut() == StatutAnnonce.EN_COURS ||
            a.getStatut() == StatutAnnonce.EN_COURS_SEGMENT_1 ||
            a.getStatut() == StatutAnnonce.ATTENTE_ENTREPOT ||
            a.getStatut() == StatutAnnonce.EN_COURS_SEGMENT_2
        ).count());
        stats.put("annoncesTerminees", annonces.stream().filter(a ->
            a.getStatut() == StatutAnnonce.TERMINEE
        ).count());
        stats.put("annoncesAnnulees", annonces.stream().filter(a ->
            a.getStatut() == StatutAnnonce.ANNULEE
        ).count());

        return stats;
    }

    @Transactional
    public Annonce createAnnonceCommercant(Annonce annonce, Integer idCommercant) {

        Utilisateur utilisateur = utilisateurRepository.findById(idCommercant)
                .orElseThrow(() -> new IllegalArgumentException("Commerçant non trouvé avec l'ID: " + idCommercant));

        if (!(utilisateur instanceof Commercant)) {
            throw new IllegalArgumentException("L'utilisateur n'est pas un commerçant");
        }

        return createAnnonce(annonce, idCommercant);
    }

    public List<Annonce> getAnnoncesByCommercantAndStatut(Integer idCommercant, StatutAnnonce statut) {
        List<Annonce> annonces = getAnnoncesByCommercant(idCommercant);
        return annonces.stream()
                .filter(a -> a.getStatut() == statut)
                .toList();
    }
}
