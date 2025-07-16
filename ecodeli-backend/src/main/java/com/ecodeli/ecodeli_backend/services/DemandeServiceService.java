package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.DemandeServiceRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DemandeServiceService {

    @Autowired
    private DemandeServiceRepository demandeServiceRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private EmailService emailService;

    public DemandeService creerDemandeService(Map<String, Object> demandeData) {
        String titre = (String) demandeData.get("titre");
        String description = (String) demandeData.get("description");
        String adresseDepart = (String) demandeData.get("adresseDepart");
        
        
        
        if (titre == null || titre.trim().isEmpty()) {
            throw new RuntimeException("Le titre est obligatoire");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new RuntimeException("La description est obligatoire");
        }
        if (adresseDepart == null || adresseDepart.trim().isEmpty()) {
            throw new RuntimeException("L'adresse de départ est obligatoire");
        }
        
        DemandeService demande = new DemandeService();
        
        demande.setTitre(titre);
        demande.setDescription(description);
        demande.setAdresseDepart(adresseDepart);
        
        if (demandeData.get("adresseArrivee") != null) {
            demande.setAdresseArrivee((String) demandeData.get("adresseArrivee"));
        }
        
        if (demandeData.get("creneauHoraire") != null) {
            demande.setCreneauHoraire((String) demandeData.get("creneauHoraire"));
        }
        
        if (demandeData.get("dateSouhaitee") != null) {
            try {
                String dateStr = (String) demandeData.get("dateSouhaitee");
                java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr);
                demande.setDateSouhaitee(localDate.atStartOfDay());
            } catch (Exception e) {
                demande.setDateSouhaitee(LocalDateTime.now().plusDays(1));
                System.err.println("Erreur parsing date: " + e.getMessage());
            }
        }
        
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) demandeData.get("categoriesService");
        if (categories != null && !categories.isEmpty()) {
            try {
                ServiceType serviceType = ServiceType.valueOf(categories.get(0));
                demande.setCategorieService(serviceType);
            } catch (IllegalArgumentException e) {
                demande.setCategorieService(ServiceType.SERVICES_DOMICILE);
            }
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> clientData = (Map<String, Object>) demandeData.get("client");
        
        if (clientData != null) {
            Integer clientId = (Integer) clientData.get("idUtilisateur");
            
            if (clientId == null) {
                throw new RuntimeException("L'ID du client est obligatoire");
            }
            
            Optional<Utilisateur> utilisateur = utilisateurRepository.findById(clientId);
            
            if (utilisateur.isPresent()) {
                if (utilisateur.get() instanceof Client) {
                    demande.setClient((Client) utilisateur.get());
                } else {
                    throw new RuntimeException("L'utilisateur n'est pas un client");
                }
            } else {
                throw new RuntimeException("Client non trouvé avec l'ID: " + clientId);
            }
        } else {
            throw new RuntimeException("Les données du client sont obligatoires");
        }
        
        if (demandeData.get("detailsSpecifiques") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) demandeData.get("detailsSpecifiques");
            demande.setDetailsSpecifiquesFromMap(details);
        }
        
        demande.setStatut(DemandeService.StatutDemande.PUBLIEE);
        
        try {
            DemandeService savedDemande = demandeServiceRepository.save(demande);
            
            try {
                if (savedDemande.getClient() != null) {
                    envoyerEmailConfirmationDemande(savedDemande);
                }
            } catch (Exception e) {
                System.err.println("Erreur envoi email: " + e.getMessage());
            }
            
            return savedDemande;
            
        } catch (Exception e) {
            System.err.println("ERREUR lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<DemandeService> getAllDemandesService() {
        return demandeServiceRepository.findAll();
    }

    public DemandeService getDemandeServiceById(Long id) {
        return demandeServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande de service non trouvée avec l'ID: " + id));
    }

    public List<DemandeService> getDemandesServiceByClient(Integer clientId) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(clientId);
        if (utilisateur.isPresent() && utilisateur.get() instanceof Client) {
            Client client = (Client) utilisateur.get();
            return demandeServiceRepository.findAll().stream()
                    .filter(demande -> demande.getClient() != null && 
                            demande.getClient().getIdUtilisateur().equals(clientId))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<DemandeService> getDemandesServiceByCategorie(String categorieService) {
        try {
            ServiceType serviceType = ServiceType.valueOf(categorieService);
            return demandeServiceRepository.findAll().stream()
                    .filter(demande -> demande.getCategorieService() == serviceType)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    public List<DemandeService> getDemandesServiceDisponibles() {
        return demandeServiceRepository.findAll().stream()
                .filter(demande -> demande.getStatut() == DemandeService.StatutDemande.PUBLIEE)
                .collect(Collectors.toList());
    }

    public DemandeService mettreAJourDemandeService(Long id, Map<String, Object> demandeData) {
        DemandeService demande = getDemandeServiceById(id);
        
        if (demande.peutEtreModifiee()) {
            if (demandeData.get("titre") != null) {
                demande.setTitre((String) demandeData.get("titre"));
            }
            if (demandeData.get("description") != null) {
                demande.setDescription((String) demandeData.get("description"));
            }
            if (demandeData.get("adresseDepart") != null) {
                demande.setAdresseDepart((String) demandeData.get("adresseDepart"));
            }
            if (demandeData.get("adresseArrivee") != null) {
                demande.setAdresseArrivee((String) demandeData.get("adresseArrivee"));
            }
            if (demandeData.get("creneauHoraire") != null) {
                demande.setCreneauHoraire((String) demandeData.get("creneauHoraire"));
            }
            
            return demandeServiceRepository.save(demande);
        } else {
            throw new RuntimeException("Cette demande ne peut plus être modifiée");
        }
    }

    public void changerStatutDemandeService(Long id, String nouveauStatut) {
        DemandeService demande = getDemandeServiceById(id);
        
        try {
            DemandeService.StatutDemande statut = DemandeService.StatutDemande.valueOf(nouveauStatut);
            demande.setStatut(statut);
            demandeServiceRepository.save(demande);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide: " + nouveauStatut);
        }
    }

    public void annulerDemandeService(Long id) {
        DemandeService demande = getDemandeServiceById(id);
        
        if (demande.peutEtreAnnulee()) {
            demande.setStatut(DemandeService.StatutDemande.ANNULEE);
            demandeServiceRepository.save(demande);
        } else {
            throw new RuntimeException("Cette demande ne peut pas être annulée");
        }
    }

    public List<DemandeService> rechercherDemandesService(Map<String, Object> filtres) {
        List<DemandeService> demandes = demandeServiceRepository.findAll();
        
        return demandes.stream()
                .filter(demande -> {
                    if (filtres.get("categorie") != null) {
                        String categorieFiltre = (String) filtres.get("categorie");
                        try {
                            ServiceType serviceType = ServiceType.valueOf(categorieFiltre);
                            if (demande.getCategorieService() != serviceType) {
                                return false;
                            }
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    }
                    
                    if (filtres.get("statut") != null) {
                        String statutFiltre = (String) filtres.get("statut");
                        try {
                            DemandeService.StatutDemande statut = DemandeService.StatutDemande.valueOf(statutFiltre);
                            if (demande.getStatut() != statut) {
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

    public Map<String, Object> getStatistiquesDemandesService() {
        List<DemandeService> toutes = demandeServiceRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", toutes.size());
        
        Map<String, Long> parStatut = toutes.stream()
                .collect(Collectors.groupingBy(
                        demande -> demande.getStatut().name(),
                        Collectors.counting()
                ));
        stats.put("parStatut", parStatut);
        
        Map<String, Long> parCategorie = toutes.stream()
                .collect(Collectors.groupingBy(
                        demande -> demande.getCategorieService().name(),
                        Collectors.counting()
                ));
        stats.put("parCategorie", parCategorie);
        
        return stats;
    }

    private void envoyerEmailConfirmationDemande(DemandeService demande) {
        if (demande.getClient() != null && demande.getClient().getEmail() != null) {
            try {
                String sujet = "Votre demande de service a été publiée - EcoDeli";
                String contenu = String.format(
                    "<h2>Votre demande de service a été publiée !</h2>" +
                    "<p>Bonjour %s,</p>" +
                    "<p>Votre demande de service <strong>\"%s\"</strong> a été publiée avec succès.</p>" +
                    "<p>Référence: <strong>#DS%d</strong></p>" +
                    "<p>Vous recevrez bientôt les candidatures de nos prestataires validés.</p>" +
                    "<p>Cordialement,<br>L'équipe EcoDeli</p>",
                    demande.getClient().getPrenom(),
                    demande.getTitre(),
                    demande.getIdDemande()
                );
                
                emailService.sendHtmlEmail(demande.getClient().getEmail(), sujet, contenu);
            } catch (Exception e) {
                System.err.println("Erreur envoi email confirmation: " + e.getMessage());
            }
        }
    }
}