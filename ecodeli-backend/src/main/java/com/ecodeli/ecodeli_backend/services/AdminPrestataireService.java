package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import com.ecodeli.ecodeli_backend.repositories.PrestataireCategorieRepository;
import com.ecodeli.ecodeli_backend.repositories.JustificatifRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminPrestataireService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PrestataireCategorieRepository prestataireCategorieRepository;

    @Autowired
    private JustificatifRepository justificatifRepository;

    /**
     * Récupérer tous les prestataires
     */
    public List<Prestataire> getAllPrestataires() {
        return utilisateurRepository.findAll().stream()
                .filter(user -> user instanceof Prestataire)
                .map(user -> (Prestataire) user)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un prestataire par ID
     */
    public Prestataire getPrestataireById(Integer id) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(id);
        if (utilisateur.isPresent() && utilisateur.get() instanceof Prestataire) {
            return (Prestataire) utilisateur.get();
        }
        throw new RuntimeException("Prestataire non trouvé avec l'ID: " + id);
    }

    /**
     * Récupérer les validations de catégories d'un prestataire
     */
    public List<PrestataireCategorie> getValidationsPrestataire(Integer prestataireId) {
        return prestataireCategorieRepository.findByPrestataireIdUtilisateur(prestataireId);
    }

    /**
     * Valider ou rejeter une catégorie pour un prestataire
     */
    public PrestataireCategorie validerCategoriePrestataire(Integer prestataireId, String categorie, String statut, String commentaire) {
        Prestataire prestataire = getPrestataireById(prestataireId);
        ServiceType serviceType;
        PrestataireCategorie.StatutValidation statutValidation;
        
        try {
            serviceType = ServiceType.valueOf(categorie);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Catégorie de service invalide: " + categorie);
        }
        
        try {
            statutValidation = PrestataireCategorie.StatutValidation.valueOf(statut);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut de validation invalide: " + statut);
        }
        
        Optional<PrestataireCategorie> existing = prestataireCategorieRepository
            .findByPrestataireIdUtilisateurAndCategorieService(prestataireId, serviceType);
        
        PrestataireCategorie validation;
        if (existing.isPresent()) {
            validation = existing.get();
        } else {
            validation = new PrestataireCategorie();
            validation.setPrestataire(prestataire);
            validation.setCategorieService(serviceType);
        }
        
        validation.setStatutValidation(statutValidation);
        validation.setCommentaireAdmin(commentaire);
        validation.setDateValidation(LocalDateTime.now());
        
        validation = prestataireCategorieRepository.save(validation);
        
        updateStatutGlobalPrestataire(prestataire);
        
        return validation;
    }

    public PrestataireCategorie definirTarifPrestataire(Integer prestataireId, String categorie, Double tarifHoraire) {
        Prestataire prestataire = getPrestataireById(prestataireId);
        
        if (tarifHoraire <= 0) {
            throw new RuntimeException("Le tarif horaire doit être positif");
        }
        
        ServiceType serviceType;
        try {
            serviceType = ServiceType.valueOf(categorie);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Catégorie de service invalide: " + categorie);
        }
        
        Optional<PrestataireCategorie> existing = prestataireCategorieRepository
            .findByPrestataireIdUtilisateurAndCategorieService(prestataireId, serviceType);
        
        PrestataireCategorie tarifCategorie;
        if (existing.isPresent()) {
            tarifCategorie = existing.get();
        } else {
            tarifCategorie = new PrestataireCategorie();
            tarifCategorie.setPrestataire(prestataire);
            tarifCategorie.setCategorieService(serviceType);
            tarifCategorie.setStatutValidation(PrestataireCategorie.StatutValidation.EN_ATTENTE);
        }
        
        tarifCategorie.setTarifHoraire(tarifHoraire);
        
        tarifCategorie = prestataireCategorieRepository.save(tarifCategorie);
        
        prestataire.setTarifHoraire(tarifHoraire);
        utilisateurRepository.save(prestataire);
        
        return tarifCategorie;
    }

    public List<Justificatif> getJustificatifsPrestataire(Integer prestataireId) {
        return justificatifRepository.findByUtilisateurIdUtilisateur(prestataireId);
    }

    public Justificatif validerJustificatif(Integer justificatifId, Boolean statut, String commentaire) {
        Optional<Justificatif> optionalJustificatif = justificatifRepository.findById(justificatifId);
        
        if (optionalJustificatif.isEmpty()) {
            throw new RuntimeException("Justificatif non trouvé avec l'ID: " + justificatifId);
        }
        
        Justificatif justificatif = optionalJustificatif.get();
        justificatif.setValidationParAd(statut);
        justificatif.setCommentaire(commentaire);
        
        return justificatifRepository.save(justificatif);
    }

    public List<Prestataire> rechercherPrestataires(Map<String, Object> filtres) {
        List<Prestataire> prestataires = getAllPrestataires();
        
        return prestataires.stream()
                .filter(prestataire -> {
                    if (filtres.get("search") != null) {
                        String search = ((String) filtres.get("search")).toLowerCase();
                        boolean matches = prestataire.getNom().toLowerCase().contains(search) ||
                                        prestataire.getPrenom().toLowerCase().contains(search) ||
                                        prestataire.getEmail().toLowerCase().contains(search) ||
                                        (prestataire.getNomEntreprise() != null && 
                                         prestataire.getNomEntreprise().toLowerCase().contains(search));
                        if (!matches) {
                            return false;
                        }
                    }
                    
                    if (filtres.get("domaineExpertise") != null) {
                        String domaine = (String) filtres.get("domaineExpertise");
                        try {
                            ServiceType serviceType = ServiceType.valueOf(domaine);
                            if (prestataire.getDomaineExpertise() != serviceType) {
                                return false;
                            }
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistiquesPrestataires() {
        List<Prestataire> prestataires = getAllPrestataires();
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalPrestataires", prestataires.size());
        
        Map<String, Long> parDomaine = prestataires.stream()
                .filter(p -> p.getDomaineExpertise() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getDomaineExpertise().name(),
                        Collectors.counting()
                ));
        stats.put("parDomaine", parDomaine);
        
        long disponibles = prestataires.stream()
                .mapToLong(p -> (p.getDisponible() != null && p.getDisponible()) ? 1 : 0)
                .sum();
        stats.put("disponibles", disponibles);
        stats.put("nonDisponibles", prestataires.size() - disponibles);
        
        OptionalDouble tarifMoyen = prestataires.stream()
                .filter(p -> p.getTarifHoraire() != null && p.getTarifHoraire() > 0)
                .mapToDouble(Prestataire::getTarifHoraire)
                .average();
        stats.put("tarifMoyen", tarifMoyen.orElse(0.0));
        
        return stats;
    }

    public byte[] exporterPrestataires(String format) {
        List<Prestataire> prestataires = getAllPrestataires();
        
        if ("csv".equals(format)) {
            return exporterCSV(prestataires);
        } else {
            throw new RuntimeException("Format d'export non supporté: " + format);
        }
    }

    private byte[] exporterCSV(List<Prestataire> prestataires) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(baos);

            writer.println("ID,Nom,Prénom,Email,Entreprise,SIRET,Domaine,Tarif Horaire,Disponible");
            
            for (Prestataire p : prestataires) {
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%.2f,%s%n",
                        p.getIdUtilisateur(),
                        escapeCSV(p.getNom()),
                        escapeCSV(p.getPrenom()),
                        escapeCSV(p.getEmail()),
                        escapeCSV(p.getNomEntreprise()),
                        escapeCSV(p.getSiret()),
                        p.getDomaineExpertise() != null ? p.getDomaineExpertise().name() : "",
                        p.getTarifHoraire() != null ? p.getTarifHoraire() : 0.0,
                        p.getDisponible() != null ? (p.getDisponible() ? "Oui" : "Non") : "Non renseigné"
                );
            }
            
            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'export CSV", e);
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public List<Prestataire> getPrestatairesParCategorie(String categorie) {
        try {
            ServiceType serviceType = ServiceType.valueOf(categorie);
            return getAllPrestataires().stream()
                    .filter(p -> p.getDomaineExpertise() == serviceType)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Récupérer les prestataires validés (avec tarif défini)
     */
    public List<Prestataire> getPrestatairesValides() {
        return getAllPrestataires().stream()
                .filter(p -> p.getTarifHoraire() != null && p.getTarifHoraire() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les prestataires en attente de validation
     */
    public List<Prestataire> getPrestatairesEnAttente() {
        return getAllPrestataires().stream()
                .filter(p -> p.getTarifHoraire() == null || p.getTarifHoraire() <= 0)
                .collect(Collectors.toList());
    }

    /**
     * Changer le statut d'un prestataire (actif/inactif)
     */
    public void changerStatutPrestataire(Integer id, Boolean actif) {
        Prestataire prestataire = getPrestataireById(id);
        prestataire.setDisponible(actif);
        utilisateurRepository.save(prestataire);
    }

    /**
     * Mettre à jour le statut global du prestataire basé sur ses validations de catégories
     */
    private void updateStatutGlobalPrestataire(Prestataire prestataire) {
        List<PrestataireCategorie> validations = prestataireCategorieRepository
            .findByPrestataireIdUtilisateur(prestataire.getIdUtilisateur());
        
        if (validations.isEmpty()) {
            prestataire.setStatutValidation(Prestataire.StatutValidationPrestataire.EN_ATTENTE);
        } else {
            boolean hasValidated = validations.stream()
                .anyMatch(v -> v.getStatutValidation() == PrestataireCategorie.StatutValidation.VALIDE);
            boolean hasRejected = validations.stream()
                .anyMatch(v -> v.getStatutValidation() == PrestataireCategorie.StatutValidation.REJETE);
            boolean allPending = validations.stream()
                .allMatch(v -> v.getStatutValidation() == PrestataireCategorie.StatutValidation.EN_ATTENTE);
            
            if (hasValidated && !hasRejected) {
                prestataire.setStatutValidation(Prestataire.StatutValidationPrestataire.VALIDE);
            } else if (hasRejected && !hasValidated) {
                prestataire.setStatutValidation(Prestataire.StatutValidationPrestataire.REJETE);
            } else if (allPending) {
                prestataire.setStatutValidation(Prestataire.StatutValidationPrestataire.EN_ATTENTE);
            } else {
                prestataire.setStatutValidation(Prestataire.StatutValidationPrestataire.EN_ATTENTE);
            }
        }
        
        utilisateurRepository.save(prestataire);
    }
}