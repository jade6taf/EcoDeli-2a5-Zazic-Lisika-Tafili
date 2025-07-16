package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrestataireService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DemandeServiceRepository demandeServiceRepository;

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private PrestataireCategorieRepository prestataireCategorieRepository;

    @Autowired
    private JustificatifRepository justificatifRepository;

    @Value("${app.upload.dir:uploads/justificatifs}")
    private String uploadDir;


    public Prestataire getPrestataireById(Integer id) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(id);
        if (utilisateur.isPresent() && utilisateur.get() instanceof Prestataire) {
            return (Prestataire) utilisateur.get();
        }
        throw new RuntimeException("Prestataire non trouvé avec l'ID: " + id);
    }


    public boolean isPrestataireValide(Integer prestataireId) {
        Prestataire prestataire = getPrestataireById(prestataireId);
        
        if (prestataire.getStatutValidation() != Prestataire.StatutValidationPrestataire.VALIDE) {
            return false;
        }
        
        Boolean hasValidatedCategory = prestataireCategorieRepository.hasValidatedCategory(prestataireId);
        
        return hasValidatedCategory != null && hasValidatedCategory;
    }


    public List<DemandeService> getDemandesDisponibles(Integer prestataireId) {
        Prestataire prestataire = getPrestataireById(prestataireId);
        
        if (!isPrestataireValide(prestataireId)) {
            return new ArrayList<>();
        }
        
        List<DemandeService> demandesPubliees = demandeServiceRepository.findByStatut(DemandeService.StatutDemande.PUBLIEE);
        
        ServiceType domaineExpertise = prestataire.getDomaineExpertise();
        if (domaineExpertise == null) {
            return new ArrayList<>();
        }
        
        return demandesPubliees.stream()
            .filter(demande -> {
                return demande.getCategorieService() != null &&
                       demande.getCategorieService() == domaineExpertise;
            })
            .filter(demande -> {
                return !candidatureRepository.existsByPrestataireAndDemande(prestataireId, demande.getIdDemande());
            })
            .sorted((d1, d2) -> d2.getDateCreation().compareTo(d1.getDateCreation()))
            .collect(Collectors.toList());
    }


    public Candidature creerCandidature(Integer prestataireId, Long demandeId,
                                      String messagePersonnalise, Integer delaiPropose) {
        Prestataire prestataire = getPrestataireById(prestataireId);
        
        if (!isPrestataireValide(prestataireId)) {
            throw new RuntimeException("Prestataire non validé - candidature impossible");
        }
        
        Optional<DemandeService> optionalDemande = demandeServiceRepository.findById(demandeId);
        if (optionalDemande.isEmpty()) {
            throw new RuntimeException("Demande de service non trouvée");
        }
        
        DemandeService demande = optionalDemande.get();
        
        if (!demande.peutRecevoirCandidatures()) {
            throw new RuntimeException("Cette demande ne peut plus recevoir de candidatures");
        }
        
        if (candidatureRepository.existsByPrestataireAndDemande(prestataireId, demandeId)) {
            throw new RuntimeException("Vous avez déjà candidaté pour cette demande");
        }
        
        Double tarifHoraire = getTarifPrestataireValidated(prestataireId, prestataire.getDomaineExpertise());
        if (tarifHoraire == null || tarifHoraire <= 0) {
            throw new RuntimeException("Tarif horaire non défini - contactez l'administration");
        }
        
        String messageTemplate = genererMessageTemplate(prestataire, demande, tarifHoraire, messagePersonnalise);
        
        Candidature candidature = new Candidature();
        candidature.setPrestataire(prestataire);
        candidature.setDemandeService(demande);
        candidature.setPrixPropose(BigDecimal.valueOf(tarifHoraire));
        candidature.setMessagePrestataire(messageTemplate);
        candidature.setDelaiPropose(delaiPropose);
        candidature.setStatut(Candidature.StatutCandidature.EN_ATTENTE);
        
        return candidatureRepository.save(candidature);
    }


    private Double getTarifPrestataireValidated(Integer prestataireId, ServiceType categorie) {
        Optional<PrestataireCategorie> optionalValidation = prestataireCategorieRepository
            .findByPrestataireIdUtilisateurAndCategorieService(prestataireId, categorie);
        
        if (optionalValidation.isPresent()) {
            PrestataireCategorie validation = optionalValidation.get();
            if (validation.getStatutValidation() == PrestataireCategorie.StatutValidation.VALIDE) {
                return validation.getTarifHoraire();
            }
        }
        
        Prestataire prestataire = getPrestataireById(prestataireId);
        return prestataire.getTarifHoraire();
    }


    private String genererMessageTemplate(Prestataire prestataire, DemandeService demande, 
                                        Double tarifHoraire, String messagePersonnalise) {
        StringBuilder message = new StringBuilder();
        
        message.append(String.format("Bonjour %s,\n\n", demande.getClient().getPrenom()));
        message.append(String.format("Je suis disponible pour votre demande \"%s\".\n", demande.getTitre()));
        message.append(String.format("Mon tarif est de %.0f€/h", tarifHoraire));
        
        ServiceType domaine = prestataire.getDomaineExpertise();
        if (domaine != null) {
            String domaineLabel = getCategorieLabel(domaine);
            message.append(String.format(" pour les services de %s", domaineLabel.toLowerCase()));
        }
        message.append(".\n\n");
        
        if (messagePersonnalise != null && !messagePersonnalise.trim().isEmpty()) {
            message.append(messagePersonnalise.trim()).append("\n\n");
        }
        
        message.append(String.format("Cordialement,\n%s %s", 
            prestataire.getPrenom(), prestataire.getNom()));
        
        if (prestataire.getNomEntreprise() != null && !prestataire.getNomEntreprise().trim().isEmpty()) {
            message.append(String.format("\n%s", prestataire.getNomEntreprise()));
        }
        
        return message.toString();
    }


    private String getCategorieLabel(ServiceType categorie) {
        Map<ServiceType, String> labels = Map.of(
            ServiceType.TRANSPORT_LIVRAISON, "Transport & Livraison",
            ServiceType.SERVICES_DOMICILE, "Services à Domicile",
            ServiceType.TRAVAUX_REPARATIONS, "Travaux & Réparations",
            ServiceType.COURSES_ACHATS, "Courses & Achats",
            ServiceType.SERVICES_PERSONNELS, "Services Personnels",
            ServiceType.EDUCATION_FORMATION, "Éducation & Formation"
        );
        return labels.getOrDefault(categorie, categorie.name());
    }


    public List<Candidature> getCandidaturesPrestataire(Integer prestataireId) {
        return candidatureRepository.findByPrestataireOrderByDateDesc(prestataireId);
    }


    public Map<String, Object> getStatistiquesPrestataire(Integer prestataireId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Object[]> statsStatut = candidatureRepository.countByStatutForPrestataire(prestataireId);
        
        long totalCandidatures = 0;
        long enAttente = 0;
        long acceptees = 0;
        long refusees = 0;
        
        for (Object[] stat : statsStatut) {
            Candidature.StatutCandidature statut = (Candidature.StatutCandidature) stat[0];
            Long count = (Long) stat[1];
            totalCandidatures += count;
            
            switch (statut) {
                case EN_ATTENTE -> enAttente = count;
                case ACCEPTEE -> acceptees = count;
                case REFUSEE -> refusees = count;
            }
        }
        
        stats.put("totalCandidatures", totalCandidatures);
        stats.put("enAttente", enAttente);
        stats.put("acceptees", acceptees);
        stats.put("refusees", refusees);
        
        if (totalCandidatures > 0) {
            double tauxAcceptation = (double) acceptees / totalCandidatures * 100;
            stats.put("tauxAcceptation", Math.round(tauxAcceptation * 100.0) / 100.0);
        } else {
            stats.put("tauxAcceptation", 0.0);
        }
        
        return stats;
    }


    public Map<String, Object> getStatutValidationDetaille(Integer prestataireId) {
        Prestataire prestataire = getPrestataireById(prestataireId);
        Map<String, Object> statut = new HashMap<>();
        
        statut.put("statutGlobal", prestataire.getStatutValidation());
        statut.put("domaineExpertise", prestataire.getDomaineExpertise());
        
        List<PrestataireCategorie> validations = prestataireCategorieRepository
            .findByPrestataireIdUtilisateur(prestataireId);
        
        statut.put("validationsCategories", validations);
        statut.put("peutCandidater", isPrestataireValide(prestataireId));
        
        return statut;
    }


    public Justificatif uploadJustificatif(Integer prestataireId, MultipartFile file, String description) {
        try {
            validateFile(file);
            
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "justificatif_" + prestataireId + "_" + System.currentTimeMillis() + extension;
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            Justificatif justificatif = new Justificatif();
            justificatif.setCheminFichier(filePath.toString());
            justificatif.setTypeJustificatif(description != null ? description : originalFilename);
            justificatif.setCommentaire("Fichier: " + originalFilename + " (Taille: " + file.getSize() + " bytes)");
            justificatif.setValidationParAd(false);
            justificatif.setDateDebut(LocalDateTime.now());
            
            Prestataire prestataire = getPrestataireById(prestataireId);
            justificatif.setUtilisateur(prestataire);
            
            return justificatifRepository.save(justificatif);
            
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier: " + e.getMessage());
        }
    }


    public List<Justificatif> getJustificatifs(Integer prestataireId) {
        return justificatifRepository.findByPrestataire(prestataireId);
    }


    public void supprimerJustificatif(Integer prestataireId, Long justificatifId) {
        Optional<Justificatif> optionalJustificatif = justificatifRepository.findById(justificatifId.intValue());
        
        if (optionalJustificatif.isEmpty()) {
            throw new RuntimeException("Justificatif non trouvé");
        }
        
        Justificatif justificatif = optionalJustificatif.get();
        
        if (!justificatif.getUtilisateur().getIdUtilisateur().equals(prestataireId)) {
            throw new RuntimeException("Accès non autorisé à ce justificatif");
        }
        
        try {
            Path filePath = Paths.get(justificatif.getCheminFichier());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
        
        justificatifRepository.delete(justificatif);
    }


    public Page<DemandeService> getDemandesPaginated(Integer prestataireId, int page, int size,
                                                   String search, String dateMin, String dateMax, String localisation) {
        Prestataire prestataire = getPrestataireById(prestataireId);
        
        if (!isPrestataireValide(prestataireId)) {
            return Page.empty();
        }
        
        ServiceType domaineExpertise = prestataire.getDomaineExpertise();
        if (domaineExpertise == null) {
            return Page.empty();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreation"));
        
        Page<DemandeService> demandes = demandeServiceRepository.findPublishedByCategorieWithFilters(
            domaineExpertise, search, localisation, pageable);
        
        List<DemandeService> demandesFiltered = demandes.getContent().stream()
            .filter(demande -> !candidatureRepository.existsByPrestataireAndDemande(prestataireId, demande.getIdDemande()))
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(demandesFiltered, pageable, demandesFiltered.size());
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }
        
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("La taille du fichier ne peut pas dépasser 10MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Nom de fichier invalide");
        }
        
        String extension = originalFilename.toLowerCase();
        if (!extension.endsWith(".pdf") && !extension.endsWith(".jpg") &&
            !extension.endsWith(".jpeg") && !extension.endsWith(".png")) {
            throw new RuntimeException("Types de fichiers autorisés: PDF, JPG, PNG");
        }
    }
}