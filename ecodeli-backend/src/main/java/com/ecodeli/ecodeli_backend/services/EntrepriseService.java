package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Entreprise;
import com.ecodeli.ecodeli_backend.repositories.EntrepriseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EntrepriseService {

    private final EntrepriseRepository entrepriseRepository;

    public EntrepriseService(EntrepriseRepository entrepriseRepository) {
        this.entrepriseRepository = entrepriseRepository;
    }

    public List<Entreprise> getAllEntreprises() {
        return entrepriseRepository.findAll();
    }

    public Optional<Entreprise> getEntrepriseById(Integer id) {
        return entrepriseRepository.findById(id);
    }

    public Optional<Entreprise> getEntrepriseBySiret(String siret) {
        return entrepriseRepository.findBySiret(siret);
    }

    public Optional<Entreprise> getEntrepriseByEmail(String email) {
        return entrepriseRepository.findByEmail(email);
    }

    @Transactional
    public Entreprise createEntreprise(Entreprise entreprise) {
        if (entreprise.getSiret() != null && entrepriseRepository.existsBySiret(entreprise.getSiret())) {
            throw new IllegalArgumentException("Une entreprise existe déjà avec ce SIRET");
        }
        if (entrepriseRepository.existsByEmail(entreprise.getEmail())) {
            throw new IllegalArgumentException("Une entreprise existe déjà avec cet email");
        }
        entreprise.setDateAjout(LocalDateTime.now());
        entreprise.setValidationParAd(false);
        return entrepriseRepository.save(entreprise);
    }

    @Transactional
    public Entreprise updateEntreprise(Integer id, Entreprise entrepriseDetails) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entreprise non trouvée avec l'ID: " + id));
        if (entrepriseDetails.getSiret() != null) {
            if (!entrepriseDetails.getSiret().equals(entreprise.getSiret()) &&
                    entrepriseRepository.existsBySiret(entrepriseDetails.getSiret())) {
                throw new IllegalArgumentException("Une entreprise existe déjà avec ce SIRET");
            }
            entreprise.setSiret(entrepriseDetails.getSiret());
        }
        if (entrepriseDetails.getStatutJuridique() != null) {
            entreprise.setStatutJuridique(entrepriseDetails.getStatutJuridique());
        }
        if (entrepriseDetails.getSecteurActivite() != null) {
            entreprise.setSecteurActivite(entrepriseDetails.getSecteurActivite());
        }
        if (entrepriseDetails.getEmail() != null) {
            if (!entrepriseDetails.getEmail().equals(entreprise.getEmail()) &&
                    entrepriseRepository.existsByEmail(entrepriseDetails.getEmail())) {
                throw new IllegalArgumentException("Une entreprise existe déjà avec cet email");
            }
            entreprise.setEmail(entrepriseDetails.getEmail());
        }
        if (entrepriseDetails.getTelephone() != null) {
            entreprise.setTelephone(entrepriseDetails.getTelephone());
        }
        if (entrepriseDetails.getAdresse() != null) {
            entreprise.setAdresse(entrepriseDetails.getAdresse());
        }
        if (entrepriseDetails.getVille() != null) {
            entreprise.setVille(entrepriseDetails.getVille());
        }
        if (entrepriseDetails.getCodePostal() != null) {
            entreprise.setCodePostal(entrepriseDetails.getCodePostal());
        }
        if (entrepriseDetails.getPays() != null) {
            entreprise.setPays(entrepriseDetails.getPays());
        }
        if (entrepriseDetails.getSiteWeb() != null) {
            entreprise.setSiteWeb(entrepriseDetails.getSiteWeb());
        }
        return entrepriseRepository.save(entreprise);
    }

    @Transactional
    public Entreprise validateEntreprise(Integer id) {
        Entreprise entreprise = entrepriseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entreprise non trouvée avec l'ID: " + id));
        entreprise.setValidationParAd(true);
        return entrepriseRepository.save(entreprise);
    }

    @Transactional
    public void deleteEntreprise(Integer id) {
        if (!entrepriseRepository.existsById(id)) {
            throw new IllegalArgumentException("Entreprise non trouvée avec l'ID: " + id);
        }
        entrepriseRepository.deleteById(id);
    }

    // public List<Entreprise> getEntreprisesByStatutJuridique(StatutJuridique statutJuridique) {
    //     return entrepriseRepository.findByStatutJuridique(statutJuridique);
    // }

    public List<Entreprise> getEntreprisesBySecteurActivite(String secteurActivite) {
        return entrepriseRepository.findBySecteurActivite(secteurActivite);
    }

    public List<Entreprise> getEntreprisesByVille(String ville) {
        return entrepriseRepository.findByVille(ville);
    }

    public List<Entreprise> getEntreprisesByValidationStatus(Boolean validationParAd) {
        return entrepriseRepository.findByValidationParAd(validationParAd);
    }
}