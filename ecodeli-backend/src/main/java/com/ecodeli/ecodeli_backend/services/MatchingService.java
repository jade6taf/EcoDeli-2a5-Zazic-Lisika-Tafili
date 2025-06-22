package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.DemandeService;
import com.ecodeli.ecodeli_backend.models.Prestataire;
import com.ecodeli.ecodeli_backend.models.ServiceType;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MatchingService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public List<Prestataire> trouverPrestatairesCompatibles(DemandeService demandeService) {
        List<Prestataire> tousLesPrestataires = utilisateurRepository.findAll()
            .stream()
            .filter(u -> u instanceof Prestataire)
            .map(u -> (Prestataire) u)
            .collect(Collectors.toList());

        return tousLesPrestataires.stream()
            .filter(prestataire -> estCompatible(prestataire, demandeService))
            .collect(Collectors.toList());
    }

    public List<Prestataire> trouverPrestatairesParCategorie(ServiceType categorieService) {
        return utilisateurRepository.findAll()
            .stream()
            .filter(u -> u instanceof Prestataire)
            .map(u -> (Prestataire) u)
            .filter(p -> p.getDomaineExpertise() == categorieService)
            .filter(p -> p.getDisponible() != null && p.getDisponible())
            .collect(Collectors.toList());
    }

    public List<Prestataire> trouverPrestatairesDisponibles() {
        return utilisateurRepository.findAll()
            .stream()
            .filter(u -> u instanceof Prestataire)
            .map(u -> (Prestataire) u)
            .filter(p -> p.getDisponible() != null && p.getDisponible())
            .collect(Collectors.toList());
    }

    private boolean estCompatible(Prestataire prestataire, DemandeService demandeService) {
        if (prestataire.getDisponible() == null || !prestataire.getDisponible()) {
            return false;
        }

        if (prestataire.getDomaineExpertise() != null &&
            prestataire.getDomaineExpertise() == demandeService.getCategorieService()) {
            return true;
        }

        return prestataire.getDomaineExpertise() == null;
    }

    public double calculerScoreCompatibilite(Prestataire prestataire, DemandeService demandeService) {
        double score = 0.0;

        if (!estCompatible(prestataire, demandeService)) {
            return 0.0;
        }

        score += 50.0;

        if (prestataire.getDomaineExpertise() == demandeService.getCategorieService()) {
            score += 30.0;
        }

        if (prestataire.getEvaluations() != null && !prestataire.getEvaluations().isEmpty()) {
            double moyenneEvaluations = prestataire.getEvaluations().stream()
                .mapToDouble(eval -> eval.getNote())
                .average()
                .orElse(0.0);
            score += (moyenneEvaluations / 5.0) * 20.0;
        }

        return Math.min(score, 100.0);
    }

    public List<Prestataire> trouverMeilleursPrestataires(DemandeService demandeService, int limite) {
        return trouverPrestatairesCompatibles(demandeService)
            .stream()
            .sorted((p1, p2) -> Double.compare(
                calculerScoreCompatibilite(p2, demandeService),
                calculerScoreCompatibilite(p1, demandeService)
            ))
            .limit(limite)
            .collect(Collectors.toList());
    }

    public boolean peutPrendreDemande(Prestataire prestataire, DemandeService demandeService) {
        return estCompatible(prestataire, demandeService);
    }
}
