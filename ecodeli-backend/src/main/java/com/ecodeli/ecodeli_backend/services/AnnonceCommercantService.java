package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.exceptions.ResourceNotFoundException;
import com.ecodeli.ecodeli_backend.exceptions.ValidationException;
import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.AnnonceCommercantRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnonceCommercantService {

    @Autowired
    private AnnonceCommercantRepository annonceRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EmailService emailService;

    private static final int MAX_ACTIVE_ANNOUNCEMENTS_PER_MERCHANT = 10;
    private static final int DEFAULT_EXPIRATION_DAYS = 30;

    public List<AnnonceCommercant> getAllAnnonces() {
        return annonceRepository.findAll();
    }

    public AnnonceCommercant getAnnonceById(Integer id) {
        return annonceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce non trouvée avec l'ID: " + id));
    }

    public List<AnnonceCommercant> getAnnoncesByCommercant(Integer commercantId) {
        return annonceRepository.findByCommercant_IdUtilisateurOrderByDateCreationDesc(commercantId);
    }

    public List<AnnonceCommercant> getAnnoncesByCategorie(CategorieAnnonce categorie) {
        return annonceRepository.findByCategorieOrderByDateCreationDesc(categorie);
    }

    public List<AnnonceCommercant> getActiveAnnouncements() {
        return annonceRepository.findActiveAnnouncements(LocalDateTime.now());
    }

    public List<AnnonceCommercant> getActiveByCategorieAnnouncements(CategorieAnnonce categorie) {
        return annonceRepository.findActiveByCategorieAnnouncements(categorie, LocalDateTime.now());
    }

    public AnnonceCommercant createAnnonce(Map<String, Object> annonceData) {
        try {
            validateBasicData(annonceData);
            
            Integer commercantId = (Integer) annonceData.get("commercantId");
            CategorieAnnonce categorie = CategorieAnnonce.valueOf((String) annonceData.get("categorie"));
            
            validateActiveAnnouncementLimit(commercantId);
            
            Commercant commercant = (Commercant) utilisateurRepository.findById(commercantId)
                .orElseThrow(() -> new ResourceNotFoundException("Commerçant non trouvé avec l'ID: " + commercantId));

            AnnonceCommercant annonce = new AnnonceCommercant();
            annonce.setCommercant(commercant);
            annonce.setCategorie(categorie);
            annonce.setTitre((String) annonceData.get("titre"));
            annonce.setDescription((String) annonceData.get("description"));
            annonce.setStatut(AnnonceCommercant.StatutAnnonce.BROUILLON);
            annonce.setStatutApprobation(AnnonceCommercant.StatutApprobation.EN_ATTENTE);

            switch (categorie) {
                case LIVRAISON_PONCTUELLE:
                    configureLivraisonPonctuelle(annonce, annonceData);
                    break;
                case SERVICE_CHARIOT:
                    configureServiceChariot(annonce, annonceData);
                    break;
                case TRANSPORT_MARCHANDISES:
                    configureTransportMarchandises(annonce, annonceData);
                    break;
            }

            annonce.setPrixEstime(calculateEstimatedPrice(annonce));
            
            if (annonce.getDateExpiration() == null) {
                annonce.setDateExpiration(LocalDateTime.now().plusDays(DEFAULT_EXPIRATION_DAYS));
            }

            return annonceRepository.save(annonce);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de l'annonce: " + e.getMessage(), e);
        }
    }

    private void configureLivraisonPonctuelle(AnnonceCommercant annonce, Map<String, Object> data) {
        validateLivraisonPonctuelleData(data);
        
        if (data.get("dateLivraisonPrecise") != null) {
            annonce.setDateLivraisonPrecise(LocalDateTime.parse((String) data.get("dateLivraisonPrecise")));
        }
        if (data.get("heureLivraisonPrecise") != null) {
            annonce.setHeureLivraisonPrecise(LocalTime.parse((String) data.get("heureLivraisonPrecise")));
        }
        annonce.setAdresseCollecte((String) data.get("adresseCollecte"));
        annonce.setAdresseDestination((String) data.get("adresseDestination"));
        annonce.setDimensionsColis((String) data.get("dimensionsColis"));
        if (data.get("poidsColis") != null) {
            annonce.setPoidsColis(new BigDecimal(data.get("poidsColis").toString()));
        }
        annonce.setInstructionsLivraison((String) data.get("instructionsLivraison"));
        annonce.setContactDestinataire((String) data.get("contactDestinataire"));
        annonce.setDisponibiliteTempsReel(Boolean.valueOf(data.get("disponibiliteTempsReel").toString()));
        
        if (annonce.getDisponibiliteTempsReel()) {
            checkRealTimeAvailability(annonce);
        }
    }

    private void checkRealTimeAvailability(AnnonceCommercant annonce) {
        throw new UnsupportedOperationException("Unimplemented method 'checkRealTimeAvailability'");
    }

    private void configureServiceChariot(AnnonceCommercant annonce, Map<String, Object> data) {
        validateServiceChariotData(data);
        
        if (data.get("heuresServiceDebut") != null) {
            annonce.setHeuresServiceDebut(LocalTime.parse((String) data.get("heuresServiceDebut")));
        }
        if (data.get("heuresServiceFin") != null) {
            annonce.setHeuresServiceFin(LocalTime.parse((String) data.get("heuresServiceFin")));
        }
        annonce.setZoneCouverture((String) data.get("zoneCouverture"));
        if (data.get("commandeMinimum") != null) {
            annonce.setCommandeMinimum(new BigDecimal(data.get("commandeMinimum").toString()));
        }
        if (data.get("tempsLivraisonMoyen") != null) {
            annonce.setTempsLivraisonMoyen((Integer) data.get("tempsLivraisonMoyen"));
        }
        annonce.setCreneauxDisponibles((String) data.get("creneauxDisponibles"));
        annonce.setJoursService((String) data.get("joursService"));
        annonce.setGestionDisponibiliteContinue(Boolean.valueOf(data.get("gestionDisponibiliteContinue").toString()));
    }

    private void configureTransportMarchandises(AnnonceCommercant annonce, Map<String, Object> data) {
        validateTransportMarchandisesData(data);
        
        if (data.get("frequenceTransport") != null) {
            annonce.setFrequenceTransport(AnnonceCommercant.FrequenceTransport.valueOf((String) data.get("frequenceTransport")));
        }
        annonce.setCategoriesMarchandises((String) data.get("categoriesMarchandises"));
        annonce.setConditionsTransport((String) data.get("conditionsTransport"));
        annonce.setItinerairesDetailles((String) data.get("itinerairesDetailles"));
        if (data.get("volumeEstime") != null) {
            annonce.setVolumeEstime(new BigDecimal(data.get("volumeEstime").toString()));
        }
        annonce.setCapaciteVehiculeRequise((String) data.get("capaciteVehiculeRequise"));
        annonce.setOptimisationRoute(Boolean.valueOf(data.get("optimisationRoute").toString()));
        annonce.setPlanificationCapacite(Boolean.valueOf(data.get("planificationCapacite").toString()));
        
        if (annonce.getOptimisationRoute()) {
            optimizeRoutes(annonce);
        }
    }

    private void optimizeRoutes(AnnonceCommercant annonce) {
        throw new UnsupportedOperationException("Unimplemented method 'optimizeRoutes'");
    }

    private void validateBasicData(Map<String, Object> data) {
        if (data.get("commercantId") == null) {
            throw new ValidationException("L'ID du commerçant est obligatoire");
        }
        if (data.get("categorie") == null) {
            throw new ValidationException("La catégorie est obligatoire");
        }
        if (data.get("titre") == null || data.get("titre").toString().trim().isEmpty()) {
            throw new ValidationException("Le titre est obligatoire");
        }
        if (data.get("titre").toString().length() > 200) {
            throw new ValidationException("Le titre ne peut pas dépasser 200 caractères");
        }
    }

    private void validateLivraisonPonctuelleData(Map<String, Object> data) {
        if (data.get("dateLivraisonPrecise") == null) {
            throw new ValidationException("La date de livraison précise est obligatoire");
        }
        if (data.get("adresseCollecte") == null || data.get("adresseCollecte").toString().trim().isEmpty()) {
            throw new ValidationException("L'adresse de collecte est obligatoire");
        }
        if (data.get("adresseDestination") == null || data.get("adresseDestination").toString().trim().isEmpty()) {
            throw new ValidationException("L'adresse de destination est obligatoire");
        }
        if (data.get("contactDestinataire") == null || data.get("contactDestinataire").toString().trim().isEmpty()) {
            throw new ValidationException("Le contact du destinataire est obligatoire");
        }
    }

    private void validateServiceChariotData(Map<String, Object> data) {
        if (data.get("heuresServiceDebut") == null || data.get("heuresServiceFin") == null) {
            throw new ValidationException("Les heures de service sont obligatoires");
        }
        if (data.get("zoneCouverture") == null || data.get("zoneCouverture").toString().trim().isEmpty()) {
            throw new ValidationException("La zone de couverture est obligatoire");
        }
        if (data.get("commandeMinimum") == null) {
            throw new ValidationException("Le montant minimum de commande est obligatoire");
        }
    }

    private void validateTransportMarchandisesData(Map<String, Object> data) {
        if (data.get("frequenceTransport") == null) {
            throw new ValidationException("La fréquence de transport est obligatoire");
        }
        if (data.get("categoriesMarchandises") == null || data.get("categoriesMarchandises").toString().trim().isEmpty()) {
            throw new ValidationException("Les catégories de marchandises sont obligatoires");
        }
        if (data.get("itinerairesDetailles") == null || data.get("itinerairesDetailles").toString().trim().isEmpty()) {
            throw new ValidationException("Les itinéraires détaillés sont obligatoires");
        }
    }

    private void validateActiveAnnouncementLimit(Integer commercantId) {
        Long activeCount = annonceRepository.countActiveAnnouncementsByCommercant(commercantId);
        if (activeCount >= MAX_ACTIVE_ANNOUNCEMENTS_PER_MERCHANT) {
            throw new ValidationException("Limite d'annonces actives atteinte (" + MAX_ACTIVE_ANNOUNCEMENTS_PER_MERCHANT + " maximum)");
        }
    }

    private BigDecimal calculateEstimatedPrice(AnnonceCommercant annonce) {
        switch (annonce.getCategorie()) {
            case LIVRAISON_PONCTUELLE:
                return calculateLivraisonPonctuellePrice(annonce);
            case SERVICE_CHARIOT:
                return calculateServiceChariotPrice(annonce);
            case TRANSPORT_MARCHANDISES:
                return calculateTransportMarchandisesPrice(annonce);
            default:
                return new BigDecimal("0.00");
        }
    }

    private BigDecimal calculateLivraisonPonctuellePrice(AnnonceCommercant annonce) {

        BigDecimal basePrice = new BigDecimal("15.00");
        BigDecimal weightPrice = annonce.getPoidsColis() != null ? 
            annonce.getPoidsColis().multiply(new BigDecimal("2.00")) : BigDecimal.ZERO;
        return basePrice.add(weightPrice);
    }

    private BigDecimal calculateServiceChariotPrice(AnnonceCommercant annonce) {
        BigDecimal basePrice = new BigDecimal("25.00");
        if (annonce.getGestionDisponibiliteContinue()) {
            basePrice = basePrice.multiply(new BigDecimal("1.2"));
        }
        return basePrice;
    }

    private BigDecimal calculateTransportMarchandisesPrice(AnnonceCommercant annonce) {

        BigDecimal basePrice = new BigDecimal("50.00");
        if (annonce.getVolumeEstime() != null) {
            basePrice = basePrice.add(annonce.getVolumeEstime().multiply(new BigDecimal("0.5")));
        }
        if (annonce.getOptimisationRoute()) {
            basePrice = basePrice.multiply(new BigDecimal("1.1"));
        }
        return basePrice;
    }

    public AnnonceCommercant updateAnnonce(Integer id, Map<String, Object> updates) {
        AnnonceCommercant annonce = getAnnonceById(id);
        
        validateUpdatePermissions(annonce, updates);
        
        if (updates.containsKey("titre")) {
            annonce.setTitre((String) updates.get("titre"));
        }
        if (updates.containsKey("description")) {
            annonce.setDescription((String) updates.get("description"));
        }
        
        updateCategorySpecificFields(annonce, updates);
        
        if (shouldRecalculatePrice(updates)) {
            annonce.setPrixEstime(calculateEstimatedPrice(annonce));
        }
        
        return annonceRepository.save(annonce);
    }

    private void validateUpdatePermissions(AnnonceCommercant annonce, Map<String, Object> updates) {
        if (annonce.getStatut() == AnnonceCommercant.StatutAnnonce.ACTIVE) {
            List<String> allowedFields = Arrays.asList("description", "creneauxDisponibles", "joursService");
            Set<String> updateKeys = updates.keySet();
            if (!allowedFields.containsAll(updateKeys)) {
                throw new ValidationException("Modifications limitées pour les annonces actives");
            }
        }
    }

    private void updateCategorySpecificFields(AnnonceCommercant annonce, Map<String, Object> updates) {
        switch (annonce.getCategorie()) {
            case LIVRAISON_PONCTUELLE:
                updateLivraisonPonctuelleFields(annonce, updates);
                break;
            case SERVICE_CHARIOT:
                updateServiceChariotFields(annonce, updates);
                break;
            case TRANSPORT_MARCHANDISES:
                updateTransportMarchandisesFields(annonce, updates);
                break;
        }
    }

    private void updateLivraisonPonctuelleFields(AnnonceCommercant annonce, Map<String, Object> updates) {
        if (updates.containsKey("dateLivraisonPrecise")) {
            annonce.setDateLivraisonPrecise(LocalDateTime.parse((String) updates.get("dateLivraisonPrecise")));
        }
        if (updates.containsKey("heureLivraisonPrecise")) {
            annonce.setHeureLivraisonPrecise(LocalTime.parse((String) updates.get("heureLivraisonPrecise")));
        }
        if (updates.containsKey("adresseCollecte")) {
            annonce.setAdresseCollecte((String) updates.get("adresseCollecte"));
        }
        if (updates.containsKey("adresseDestination")) {
            annonce.setAdresseDestination((String) updates.get("adresseDestination"));
        }
        if (updates.containsKey("instructionsLivraison")) {
            annonce.setInstructionsLivraison((String) updates.get("instructionsLivraison"));
        }
    }

    private void updateServiceChariotFields(AnnonceCommercant annonce, Map<String, Object> updates) {
        if (updates.containsKey("heuresServiceDebut")) {
            annonce.setHeuresServiceDebut(LocalTime.parse((String) updates.get("heuresServiceDebut")));
        }
        if (updates.containsKey("heuresServiceFin")) {
            annonce.setHeuresServiceFin(LocalTime.parse((String) updates.get("heuresServiceFin")));
        }
        if (updates.containsKey("zoneCouverture")) {
            annonce.setZoneCouverture((String) updates.get("zoneCouverture"));
        }
        if (updates.containsKey("creneauxDisponibles")) {
            annonce.setCreneauxDisponibles((String) updates.get("creneauxDisponibles"));
        }
        if (updates.containsKey("joursService")) {
            annonce.setJoursService((String) updates.get("joursService"));
        }
    }

    private void updateTransportMarchandisesFields(AnnonceCommercant annonce, Map<String, Object> updates) {
        if (updates.containsKey("frequenceTransport")) {
            annonce.setFrequenceTransport(AnnonceCommercant.FrequenceTransport.valueOf((String) updates.get("frequenceTransport")));
        }
        if (updates.containsKey("categoriesMarchandises")) {
            annonce.setCategoriesMarchandises((String) updates.get("categoriesMarchandises"));
        }
        if (updates.containsKey("conditionsTransport")) {
            annonce.setConditionsTransport((String) updates.get("conditionsTransport"));
        }
        if (updates.containsKey("itinerairesDetailles")) {
            annonce.setItinerairesDetailles((String) updates.get("itinerairesDetailles"));
        }
    }

    private boolean shouldRecalculatePrice(Map<String, Object> updates) {
        List<String> priceAffectingFields = Arrays.asList(
            "poidsColis", "volumeEstime", "commandeMinimum", "frequenceTransport",
            "optimisationRoute", "gestionDisponibiliteContinue"
        );
        return updates.keySet().stream().anyMatch(priceAffectingFields::contains);
    }

    public AnnonceCommercant publishAnnonce(Integer id) {
        AnnonceCommercant annonce = getAnnonceById(id);
        
        if (annonce.getStatut() != AnnonceCommercant.StatutAnnonce.BROUILLON) {
            throw new ValidationException("Seules les annonces en brouillon peuvent être publiées");
        }
        
        if (annonce.getStatutApprobation() != AnnonceCommercant.StatutApprobation.APPROUVEE) {
            throw new ValidationException("L'annonce doit être approuvée avant publication");
        }
        
        annonce.setStatut(AnnonceCommercant.StatutAnnonce.PUBLIEE);
        annonce.setDatePublication(LocalDateTime.now());
        
        return annonceRepository.save(annonce);
    }

    public AnnonceCommercant approveAnnonce(Integer id, Integer adminId, String commentaire) {
        AnnonceCommercant annonce = getAnnonceById(id);
        Admin admin = (Admin) utilisateurRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin non trouvé"));
        
        annonce.setStatutApprobation(AnnonceCommercant.StatutApprobation.APPROUVEE);
        annonce.setAdminApprobateur(admin);
        annonce.setCommentaireApprobation(commentaire);
        annonce.setDateApprobation(LocalDateTime.now());
        
        sendApprovalNotification(annonce, true);
        
        return annonceRepository.save(annonce);
    }

    public AnnonceCommercant rejectAnnonce(Integer id, Integer adminId, String commentaire) {
        AnnonceCommercant annonce = getAnnonceById(id);
        Admin admin = (Admin) utilisateurRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin non trouvé"));
        
        annonce.setStatutApprobation(AnnonceCommercant.StatutApprobation.REJETEE);
        annonce.setAdminApprobateur(admin);
        annonce.setCommentaireApprobation(commentaire);
        annonce.setDateApprobation(LocalDateTime.now());
        
        sendApprovalNotification(annonce, false);
        
        return annonceRepository.save(annonce);
    }

    private void sendApprovalNotification(AnnonceCommercant annonce, boolean approved) {
        try {
            String subject = approved ? "Annonce approuvée" : "Annonce rejetée";
            String status = approved ? "approuvée" : "rejetée";
            
            String content = String.format(
                "Bonjour %s,\n\nVotre annonce \"%s\" a été %s.\n\nCommentaire: %s",
                annonce.getCommercant().getPrenom(),
                annonce.getTitre(),
                status,
                annonce.getCommentaireApprobation()
            );
            
            emailService.sendHtmlEmail(annonce.getCommercant().getEmail(), subject, content);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
    }

    public void deleteAnnonce(Integer id) {
        AnnonceCommercant annonce = getAnnonceById(id);
        
        if (annonce.getStatut() == AnnonceCommercant.StatutAnnonce.ACTIVE) {
            throw new ValidationException("Impossible de supprimer une annonce active");
        }
        
        annonceRepository.delete(annonce);
    }

    public List<AnnonceCommercant> searchAnnonces(String searchTerm, CategorieAnnonce categorie, 
                                                  AnnonceCommercant.StatutAnnonce statut, Integer commercantId) {
        return annonceRepository.findWithFilters(categorie, statut, commercantId, searchTerm);
    }

    public Map<String, Object> getAnnonceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Object[]> categorieStats = annonceRepository.getStatisticsByCategorie();
        Map<String, Long> categorieMap = new HashMap<>();
        for (Object[] stat : categorieStats) {
            categorieMap.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("parCategorie", categorieMap);
        
        List<Object[]> statutStats = annonceRepository.getStatisticsByStatut();
        Map<String, Long> statutMap = new HashMap<>();
        for (Object[] stat : statutStats) {
            statutMap.put(stat[0].toString(), (Long) stat[1]);
        }
        stats.put("parStatut", statutMap);
        
        return stats;
    }

    public List<AnnonceCommercant> getPendingApproval() {
        return annonceRepository.findPendingApproval();
    }

    public List<AnnonceCommercant> getExpiringAnnouncements(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        return annonceRepository.findExpiringAnnouncements(now, futureDate);
    }

    public Map<String, Object> getContextualHelp(CategorieAnnonce categorie) {
        Map<String, Object> help = new HashMap<>();
        
        switch (categorie) {
            case LIVRAISON_PONCTUELLE:
                help.put("titre", "Créer une annonce de livraison ponctuelle");
                help.put("description", "Pour des besoins de livraison uniques ou occasionnels");
                help.put("etapes", Arrays.asList(
                    "Définir la date et l'heure précises",
                    "Saisir les adresses de collecte et destination",
                    "Renseigner les dimensions et poids du colis",
                    "Ajouter les instructions spéciales"
                ));
                break;
            case SERVICE_CHARIOT:
                help.put("titre", "Créer un service de chariot");
                help.put("description", "Pour les commerçants proposant des services de livraison à domicile");
                help.put("etapes", Arrays.asList(
                    "Définir les heures de service",
                    "Délimiter la zone de couverture",
                    "Fixer le montant minimum de commande",
                    "Configurer les créneaux disponibles"
                ));
                break;
            case TRANSPORT_MARCHANDISES:
                help.put("titre", "Créer une annonce de transport de marchandises");
                help.put("description", "Pour les besoins de transport régulier ou en volume");
                help.put("etapes", Arrays.asList(
                    "Choisir la fréquence de transport",
                    "Détailler les catégories de marchandises",
                    "Définir les conditions de transport",
                    "Planifier les itinéraires"
                ));
                break;
        }
        
        return help;
    }
}