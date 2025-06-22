package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Candidature;
import com.ecodeli.ecodeli_backend.models.Candidature.StatutCandidature;
import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.repositories.CandidatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CandidatureService {

    @Autowired
    private CandidatureRepository candidatureRepository;

    @Autowired
    private DemandeServiceService demandeServiceService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private com.ecodeli.ecodeli_backend.repositories.ServiceRepository serviceRepository;

    public Candidature creerCandidature(Candidature candidature) {

        if (candidature.getDemandeService() == null) {
            throw new IllegalArgumentException("La demande de service est obligatoire");
        }
        if (candidature.getPrestataire() == null) {
            throw new IllegalArgumentException("Le prestataire est obligatoire");
        }
        if (candidature.getPrixPropose() == null || candidature.getPrixPropose().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix proposé doit être positif");
        }

        if (!candidature.getDemandeService().peutRecevoirCandidatures()) {
            throw new IllegalStateException("Cette demande ne peut plus recevoir de candidatures");
        }

        if (candidatureRepository.existsByDemandeServiceAndPrestataire(
                candidature.getDemandeService(), candidature.getPrestataire())) {
            throw new IllegalStateException("Vous avez déjà candidaté pour cette demande");
        }

        candidature.setStatut(StatutCandidature.EN_ATTENTE);
        Candidature nouvelleCandidature = candidatureRepository.save(candidature);

        demandeServiceService.marquerCandidaturesRecues(candidature.getDemandeService().getIdDemande());

        return nouvelleCandidature;
    }

    public Candidature accepterCandidature(Long candidatureId, String commentaireClient) {
        Candidature candidature = getCandidatureById(candidatureId);

        if (!candidature.peutEtreAcceptee()) {
            throw new IllegalStateException("Cette candidature ne peut pas être acceptée");
        }

        candidature.accepter(commentaireClient);
        Candidature candidatureAcceptee = candidatureRepository.save(candidature);

        List<Candidature> autresCandidatures = candidatureRepository
            .findCandidaturesEnAttenteParDemande(candidature.getDemandeService());

        for (Candidature autre : autresCandidatures) {
            if (!autre.getIdCandidature().equals(candidatureId)) {
                autre.refuser("Candidature automatiquement refusée - Un autre prestataire a été sélectionné");
                candidatureRepository.save(autre);
            }
        }

        demandeServiceService.marquerPrestataireSelectionne(candidature.getDemandeService().getIdDemande());

        try {
            serviceService.creerServiceDepuisCandidature(candidatureAcceptee);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création automatique du service: " + e.getMessage());
        }

        return candidatureAcceptee;
    }

    public Candidature refuserCandidature(Long candidatureId, String commentaireClient) {
        Candidature candidature = getCandidatureById(candidatureId);

        if (!candidature.peutEtreRefusee()) {
            throw new IllegalStateException("Cette candidature ne peut pas être refusée");
        }

        candidature.refuser(commentaireClient);
        return candidatureRepository.save(candidature);
    }

    @Transactional(readOnly = true)
    public Candidature getCandidatureById(Long id) {
        return candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Candidature> getCandidaturesParDemande(DemandeService demandeService) {
        return candidatureRepository.findByDemandeServiceOrderByDateCandidatureDesc(demandeService);
    }

    @Transactional(readOnly = true)
    public List<Candidature> getCandidaturesParPrestataire(Prestataire prestataire) {
        return candidatureRepository.findByPrestataireOrderByDateCandidatureDesc(prestataire);
    }

    @Transactional(readOnly = true)
    public List<Candidature> getCandidaturesEnAttenteParDemande(DemandeService demandeService) {
        return candidatureRepository.findCandidaturesEnAttenteParDemande(demandeService);
    }

    @Transactional(readOnly = true)
    public List<Candidature> getCandidaturesAccepteesPrestataire(Prestataire prestataire) {
        return candidatureRepository.findCandidaturesAccepteesPrestataire(prestataire);
    }

    @Transactional(readOnly = true)
    public List<Candidature> getCandidaturesParStatut(StatutCandidature statut) {
        return candidatureRepository.findByStatutOrderByDateCandidatureDesc(statut);
    }

    @Transactional(readOnly = true)
    public List<Candidature> getCandidaturesPrestataireParStatut(Prestataire prestataire, StatutCandidature statut) {
        return candidatureRepository.findByPrestataireAndStatutOrderByDateCandidatureDesc(prestataire, statut);
    }

    @Transactional(readOnly = true)
    public boolean peutCandidater(DemandeService demandeService, Prestataire prestataire) {
        if (!demandeService.peutRecevoirCandidatures()) {
            return false;
        }

        return !candidatureRepository.existsByDemandeServiceAndPrestataire(demandeService, prestataire);
    }

    @Transactional(readOnly = true)
    public Long compterCandidaturesParDemande(DemandeService demandeService) {
        return candidatureRepository.countByDemandeService(demandeService);
    }

    @Transactional(readOnly = true)
    public Long compterCandidaturesParPrestataire(Prestataire prestataire) {
        return candidatureRepository.countByPrestataire(prestataire);
    }

    @Transactional(readOnly = true)
    public Long compterCandidaturesParStatut(StatutCandidature statut) {
        return candidatureRepository.countByStatut(statut);
    }

    public com.ecodeli.ecodeli_backend.models.Service creerServiceDepuisCandidature(Long candidatureId) {
        Candidature candidature = getCandidatureById(candidatureId);

        if (candidature.getStatut() != StatutCandidature.ACCEPTEE) {
            throw new IllegalStateException("Seules les candidatures acceptées peuvent générer un service");
        }

        return com.ecodeli.ecodeli_backend.models.Service.creerDepuisCandidature(candidature);
    }

    @Transactional(readOnly = true)
    public List<Candidature> getCandidaturesActivesParPrestataire(Prestataire prestataire) {
        List<Candidature> toutesCandidatures = candidatureRepository.findByPrestataireOrderByDateCandidatureDesc(prestataire);

        return toutesCandidatures.stream()
            .filter(candidature -> {
                if (candidature.getStatut() != StatutCandidature.ACCEPTEE) {
                    return true;
                }
                try {
                    com.ecodeli.ecodeli_backend.models.Service service = serviceRepository
                        .findByCandidature_IdCandidature(candidature.getIdCandidature())
                        .orElse(null);

                    return service == null || !service.estTermine();
                } catch (Exception e) {
                    return true;
                }
            })
            .toList();
    }
}
