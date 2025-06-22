package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.models.DemandeService.StatutDemande;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import com.ecodeli.ecodeli_backend.models.Client;
import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.repositories.DemandeServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class DemandeServiceService {

    @Autowired
    private DemandeServiceRepository demandeServiceRepository;

    @Autowired
    private MatchingService matchingService;

    public DemandeService creerDemande(DemandeService demande) {
        if (demande.getTitre() == null || demande.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de la demande est obligatoire");
        }
        if (demande.getDescription() == null || demande.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La description de la demande est obligatoire");
        }
        if (demande.getCategorieService() == null) {
            throw new IllegalArgumentException("La catégorie de service est obligatoire");
        }
        if (demande.getClient() == null) {
            throw new IllegalArgumentException("Le client est obligatoire");
        }

        demande.setStatut(StatutDemande.PUBLIEE);
        return demandeServiceRepository.save(demande);
    }

    public DemandeService modifierDemande(Long id, DemandeService demandeModifiee) {
        DemandeService demande = getDemandeById(id);

        if (!demande.peutEtreModifiee()) {
            throw new IllegalStateException("Cette demande ne peut plus être modifiée");
        }

        demande.setTitre(demandeModifiee.getTitre());
        demande.setDescription(demandeModifiee.getDescription());
        demande.setTypeServiceSpecifique(demandeModifiee.getTypeServiceSpecifique());
        demande.setAdresseDepart(demandeModifiee.getAdresseDepart());
        demande.setAdresseArrivee(demandeModifiee.getAdresseArrivee());
        demande.setDateSouhaitee(demandeModifiee.getDateSouhaitee());
        demande.setCreneauHoraire(demandeModifiee.getCreneauHoraire());
        demande.setBudgetMin(demandeModifiee.getBudgetMin());
        demande.setBudgetMax(demandeModifiee.getBudgetMax());
        demande.setPhotosUrls(demandeModifiee.getPhotosUrls());

        return demandeServiceRepository.save(demande);
    }

    public void annulerDemande(Long id) {
        DemandeService demande = getDemandeById(id);

        if (!demande.peutEtreAnnulee()) {
            throw new IllegalStateException("Cette demande ne peut pas être annulée");
        }

        demande.setStatut(StatutDemande.ANNULEE);
        demandeServiceRepository.save(demande);
    }

    public void marquerCandidaturesRecues(Long id) {
        DemandeService demande = getDemandeById(id);

        if (demande.getStatut() == StatutDemande.PUBLIEE && demande.getNombreCandidatures() > 0) {
            demande.setStatut(StatutDemande.CANDIDATURES_RECUES);
            demandeServiceRepository.save(demande);
        }
    }

    public void marquerPrestataireSelectionne(Long id) {
        DemandeService demande = getDemandeById(id);
        demande.setStatut(StatutDemande.PRESTATAIRE_SELECTIONNE);
        demandeServiceRepository.save(demande);
    }

    public void marquerEnCours(Long id) {
        DemandeService demande = getDemandeById(id);
        demande.setStatut(StatutDemande.EN_COURS);
        demandeServiceRepository.save(demande);
    }

    public void marquerTerminee(Long id) {
        DemandeService demande = getDemandeById(id);
        demande.setStatut(StatutDemande.TERMINEE);
        demandeServiceRepository.save(demande);
    }

    @Transactional(readOnly = true)
    public DemandeService getDemandeById(Long id) {
        return demandeServiceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Demande de service non trouvée avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<DemandeService> getDemandesClient(Client client) {
        return demandeServiceRepository.findByClientOrderByDateCreationDesc(client);
    }

    @Transactional(readOnly = true)
    public List<DemandeService> getDemandesDisponiblesPourCandidatures() {
        List<StatutDemande> statutsDisponibles = Arrays.asList(
            StatutDemande.PUBLIEE,
            StatutDemande.CANDIDATURES_RECUES
        );
        return demandeServiceRepository.findDemandesDisponiblesPourCandidatures(statutsDisponibles);
    }

    @Transactional(readOnly = true)
    public List<DemandeService> getDemandesParCategorie(ServiceType categorie) {
        return demandeServiceRepository.findByCategorieServiceOrderByDateCreationDesc(categorie);
    }

    @Transactional(readOnly = true)
    public List<DemandeService> getDemandesParStatut(StatutDemande statut) {
        return demandeServiceRepository.findByStatutOrderByDateCreationDesc(statut);
    }

    @Transactional(readOnly = true)
    public List<DemandeService> getDemandesRecentes(int nombreJours) {
        LocalDateTime dateDebut = LocalDateTime.now().minusDays(nombreJours);
        return demandeServiceRepository.findByDateCreationAfterOrderByDateCreationDesc(dateDebut);
    }

    @Transactional(readOnly = true)
    public Long compterDemandesParStatut(StatutDemande statut) {
        return demandeServiceRepository.countByStatut(statut);
    }

    @Transactional(readOnly = true)
    public Long compterDemandesClient(Client client) {
        return demandeServiceRepository.countByClient(client);
    }

    @Transactional(readOnly = true)
    public List<DemandeService> rechercherDemandesParBudget(Double budgetMin, Double budgetMax) {
        return demandeServiceRepository.findByBudgetRange(budgetMin, budgetMax);
    }

    @Transactional(readOnly = true)
    public boolean peutCreerNouvelleDemande(Client client) {
        long demandesPubliees = demandeServiceRepository.findByClientAndStatutOrderByDateCreationDesc(
            client, StatutDemande.PUBLIEE).size();
        long demandesCandidatures = demandeServiceRepository.findByClientAndStatutOrderByDateCreationDesc(
            client, StatutDemande.CANDIDATURES_RECUES).size();

        long demandesActives = demandesPubliees + demandesCandidatures;
        return demandesActives < 5;
    }

    @Transactional(readOnly = true)
    public List<DemandeService> getDemandesCompatiblesPrestataire(Prestataire prestataire) {
        List<DemandeService> demandesDisponibles = getDemandesDisponiblesPourCandidatures();
        return demandesDisponibles.stream()
            .filter(demande -> matchingService.peutPrendreDemande(prestataire, demande))
            .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DemandeService> getDemandesParCategorieDisponibles(ServiceType categorieService) {
        List<StatutDemande> statutsDisponibles = Arrays.asList(
            StatutDemande.PUBLIEE,
            StatutDemande.CANDIDATURES_RECUES
        );
        return demandeServiceRepository.findByCategorieServiceAndStatutOrderByDateCreationDesc(
            categorieService, statutsDisponibles.get(0));
    }
}
