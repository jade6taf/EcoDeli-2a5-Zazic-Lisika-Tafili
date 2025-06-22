package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.models.Service.StatutService;
import com.ecodeli.ecodeli_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DemandeServiceRepository demandeServiceRepository;

    public com.ecodeli.ecodeli_backend.models.Service creerServiceDepuisCandidature(Candidature candidature) {
        if (candidature.getStatut() != Candidature.StatutCandidature.ACCEPTEE) {
            throw new IllegalArgumentException("La candidature doit être acceptée pour créer un service");
        }

        Optional<com.ecodeli.ecodeli_backend.models.Service> serviceExistant =
            serviceRepository.findByCandidature_IdCandidature(candidature.getIdCandidature());
        if (serviceExistant.isPresent()) {
            return serviceExistant.get();
        }

        com.ecodeli.ecodeli_backend.models.Service service =
            com.ecodeli.ecodeli_backend.models.Service.creerDepuisCandidature(candidature);

        service = serviceRepository.save(service);

        DemandeService demande = candidature.getDemandeService();
        demande.setStatut(DemandeService.StatutDemande.PRESTATAIRE_SELECTIONNE);
        demandeServiceRepository.save(demande);

        return service;
    }

    public com.ecodeli.ecodeli_backend.models.Service commencerService(Long idService, Integer idPrestataire) {
        com.ecodeli.ecodeli_backend.models.Service service = getServiceById(idService);

        if (!service.getPrestataire().getIdUtilisateur().equals(idPrestataire)) {
            throw new IllegalArgumentException("Seul le prestataire assigné peut commencer le service");
        }

        service.commencer();
        service = serviceRepository.save(service);

        DemandeService demande = service.getDemandeService();
        demande.setStatut(DemandeService.StatutDemande.EN_COURS);
        demandeServiceRepository.save(demande);

        return service;
    }

    public com.ecodeli.ecodeli_backend.models.Service terminerService(Long idService, Integer idPrestataire, String notesPrestataire) {
        com.ecodeli.ecodeli_backend.models.Service service = getServiceById(idService);

        if (!service.getPrestataire().getIdUtilisateur().equals(idPrestataire)) {
            throw new IllegalArgumentException("Seul le prestataire assigné peut terminer le service");
        }

        service.terminer();
        if (notesPrestataire != null && !notesPrestataire.trim().isEmpty()) {
            service.setNotesPrestataire(notesPrestataire);
        }
        service = serviceRepository.save(service);

        DemandeService demande = service.getDemandeService();
        demande.setStatut(DemandeService.StatutDemande.TERMINEE);
        demandeServiceRepository.save(demande);
        return service;
    }

    public com.ecodeli.ecodeli_backend.models.Service annulerService(Long idService, Integer idUtilisateur, String raison) {
        com.ecodeli.ecodeli_backend.models.Service service = getServiceById(idService);

        boolean estClient = service.getClient().getIdUtilisateur().equals(idUtilisateur);
        boolean estPrestataire = service.getPrestataire().getIdUtilisateur().equals(idUtilisateur);

        if (!estClient && !estPrestataire) {
            throw new IllegalArgumentException("Seul le client ou le prestataire peut annuler le service");
        }

        service.annuler();
        if (raison != null && !raison.trim().isEmpty()) {
            if (estClient) {
                service.setNotesClient(raison);
            } else {
                service.setNotesPrestataire(raison);
            }
        }
        service = serviceRepository.save(service);

        DemandeService demande = service.getDemandeService();
        demande.setStatut(DemandeService.StatutDemande.ANNULEE);
        demandeServiceRepository.save(demande);

        return service;
    }

    public com.ecodeli.ecodeli_backend.models.Service getServiceById(Long idService) {
        return serviceRepository.findById(idService)
            .orElseThrow(() -> new RuntimeException("Service non trouvé avec l'ID: " + idService));
    }

    public List<com.ecodeli.ecodeli_backend.models.Service> getServicesEnCoursPrestataire(Integer idPrestataire) {
        Prestataire prestataire = (Prestataire) utilisateurRepository.findById(idPrestataire)
            .orElseThrow(() -> new RuntimeException("Prestataire non trouvé"));
        return serviceRepository.findServicesEnCoursByPrestataire(prestataire);
    }

    public List<com.ecodeli.ecodeli_backend.models.Service> getServicesTerminesPrestataire(Integer idPrestataire) {
        Prestataire prestataire = (Prestataire) utilisateurRepository.findById(idPrestataire)
            .orElseThrow(() -> new RuntimeException("Prestataire non trouvé"));
        return serviceRepository.findServicesTerminesByPrestataire(prestataire);
    }

    public List<com.ecodeli.ecodeli_backend.models.Service> getServicesPrestataire(Integer idPrestataire) {
        Prestataire prestataire = (Prestataire) utilisateurRepository.findById(idPrestataire)
            .orElseThrow(() -> new RuntimeException("Prestataire non trouvé"));
        return serviceRepository.findByPrestataireOrderByDateCreationDesc(prestataire);
    }

    public List<com.ecodeli.ecodeli_backend.models.Service> getServicesClient(Integer idClient) {
        Client client = (Client) utilisateurRepository.findById(idClient)
            .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        return serviceRepository.findByClientOrderByDateCreationDesc(client);
    }

    public List<com.ecodeli.ecodeli_backend.models.Service> getServicesClientParStatut(Integer idClient, StatutService statut) {
        Client client = (Client) utilisateurRepository.findById(idClient)
            .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        return serviceRepository.findByClientAndStatutOrderByDateCreationDesc(client, statut);
    }

    public StatistiquesPrestataire getStatistiquesPrestataire(Integer idPrestataire) {
        Prestataire prestataire = (Prestataire) utilisateurRepository.findById(idPrestataire)
            .orElseThrow(() -> new RuntimeException("Prestataire non trouvé"));

        Long total = serviceRepository.countByPrestataire(prestataire);
        Long enCours = serviceRepository.countByPrestataireAndStatut(prestataire, StatutService.EN_COURS);
        Long termines = serviceRepository.countByPrestataireAndStatut(prestataire, StatutService.TERMINE);
        Long annules = serviceRepository.countByPrestataireAndStatut(prestataire, StatutService.ANNULE);
        Double chiffresAffaires = serviceRepository.calculateChiffresAffairesPrestataire(prestataire);

        return new StatistiquesPrestataire(total, enCours, termines, annules, chiffresAffaires);
    }

    public List<com.ecodeli.ecodeli_backend.models.Service> getServicesUrgentsPrestataire(Integer idPrestataire) {
        Prestataire prestataire = (Prestataire) utilisateurRepository.findById(idPrestataire)
            .orElseThrow(() -> new RuntimeException("Prestataire non trouvé"));

        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime limite = maintenant.plusHours(24);

        return serviceRepository.findServicesUrgents(maintenant, limite)
            .stream()
            .filter(service -> service.getPrestataire().equals(prestataire))
            .toList();
    }

    public com.ecodeli.ecodeli_backend.models.Service mettreAJourNotes(Long idService, Integer idUtilisateur, String notes) {
        com.ecodeli.ecodeli_backend.models.Service service = getServiceById(idService);

        boolean estClient = service.getClient().getIdUtilisateur().equals(idUtilisateur);
        boolean estPrestataire = service.getPrestataire().getIdUtilisateur().equals(idUtilisateur);

        if (!estClient && !estPrestataire) {
            throw new IllegalArgumentException("Seul le client ou le prestataire peut modifier les notes");
        }

        if (estClient) {
            service.setNotesClient(notes);
        } else {
            service.setNotesPrestataire(notes);
        }

        return serviceRepository.save(service);
    }

    public com.ecodeli.ecodeli_backend.models.Service mettreAJourProgression(Long idService, Integer idPrestataire, Integer progression, String notes) {
        com.ecodeli.ecodeli_backend.models.Service service = getServiceById(idService);

        if (!service.getPrestataire().getIdUtilisateur().equals(idPrestataire)) {
            throw new IllegalArgumentException("Seul le prestataire assigné peut mettre à jour la progression");
        }

        if (!service.estEnCours()) {
            throw new IllegalStateException("La progression ne peut être mise à jour que pour un service en cours");
        }

        if (progression < 0 || progression > 100) {
            throw new IllegalArgumentException("La progression doit être comprise entre 0 et 100");
        }

        service.setProgression(progression);

        if (notes != null && !notes.trim().isEmpty()) {
            String notesActuelles = service.getNotesPrestataire();
            String nouvellesNotes = notesActuelles != null
                ? notesActuelles + "\n[" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "] " + notes
                : "[" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "] " + notes;
            service.setNotesPrestataire(nouvellesNotes);
        }

        return serviceRepository.save(service);
    }

    public static class StatistiquesPrestataire {
        private Long totalServices;
        private Long servicesEnCours;
        private Long servicesTermines;
        private Long servicesAnnules;
        private Double chiffresAffaires;

        public StatistiquesPrestataire(Long total, Long enCours, Long termines, Long annules, Double ca) {
            this.totalServices = total;
            this.servicesEnCours = enCours;
            this.servicesTermines = termines;
            this.servicesAnnules = annules;
            this.chiffresAffaires = ca != null ? ca : 0.0;
        }

        public Long getTotalServices() { return totalServices; }
        public Long getServicesEnCours() { return servicesEnCours; }
        public Long getServicesTermines() { return servicesTermines; }
        public Long getServicesAnnules() { return servicesAnnules; }
        public Double getChiffresAffaires() { return chiffresAffaires; }
    }
}
