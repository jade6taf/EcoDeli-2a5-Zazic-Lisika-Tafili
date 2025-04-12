package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Colis;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.repositories.ColisRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ColisService {

    private final ColisRepository colisRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ColisService(ColisRepository colisRepository, UtilisateurRepository utilisateurRepository) {
        this.colisRepository = colisRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<Colis> getAllColis() {
        return colisRepository.findAll();
    }

    public Optional<Colis> getColisById(Integer id) {
        return colisRepository.findById(id);
    }

    public List<Colis> getColisByExpediteur(Integer idExpediteur) {
        return colisRepository.findByExpediteurIdUtilisateur(idExpediteur);
    }

    public List<Colis> getColisByDestinataire(Integer idDestinataire) {
        return colisRepository.findByDestinataireIdUtilisateur(idDestinataire);
    }

    public List<Colis> getColisByType(String type) {
        return colisRepository.findByType(type);
    }

    public List<Colis> searchColisByPoidRange(BigDecimal poidMin, BigDecimal poidMax) {
        return colisRepository.findByPoidRange(poidMin, poidMax);
    }

    public List<Colis> searchColisByTailleRange(Integer tailleMin, Integer tailleMax) {
        return colisRepository.findByTailleRange(tailleMin, tailleMax);
    }

    public List<Colis> getActivePackages() {
        return colisRepository.findActivePackages(LocalDateTime.now());
    }

    @Transactional
    public Colis createColis(Colis colis, Integer idExpediteur, Integer idDestinataire) {

        Utilisateur expediteur = utilisateurRepository.findById(idExpediteur)
                .orElseThrow(() -> new IllegalArgumentException("Expéditeur non trouvé avec l'ID: " + idExpediteur));
        Utilisateur destinataire = utilisateurRepository.findById(idDestinataire)
                .orElseThrow(() -> new IllegalArgumentException("Destinataire non trouvé avec l'ID: " + idDestinataire));

        if (colis.getDateDebut() != null && colis.getDateFin() != null) {
            if (colis.getDateDebut().isAfter(colis.getDateFin())) {
                throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
            }
        }
        if (colis.getPoid() == null || colis.getPoid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le poids du colis doit être positif");
        }
        if (colis.getTaille() == null || colis.getTaille() <= 0) {
            throw new IllegalArgumentException("La taille du colis doit être positive");
        }

        colis.setExpediteur(expediteur);
        colis.setDestinataire(destinataire);

        if (colis.getDateDebut() == null) {
            colis.setDateDebut(LocalDateTime.now());
        }
        if (colis.getDateFin() == null) {
            // Par défaut, fin dans 7 jours
            colis.setDateFin(LocalDateTime.now().plusDays(7));
        }
        return colisRepository.save(colis);
    }

    @Transactional
    public Colis updateColis(Integer id, Colis colisDetails) {

        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Colis non trouvé avec l'ID: " + id));

        if (colisDetails.getDateDebut() != null && colisDetails.getDateFin() != null) {
            if (colisDetails.getDateDebut().isAfter(colisDetails.getDateFin())) {
                throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
            }
            colis.setDateDebut(colisDetails.getDateDebut());
            colis.setDateFin(colisDetails.getDateFin());
        } else if (colisDetails.getDateDebut() != null) {
            if (colisDetails.getDateDebut().isAfter(colis.getDateFin())) {
                throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
            }
            colis.setDateDebut(colisDetails.getDateDebut());
        } else if (colisDetails.getDateFin() != null) {
            if (colis.getDateDebut().isAfter(colisDetails.getDateFin())) {
                throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début");
            }
            colis.setDateFin(colisDetails.getDateFin());
        }

        if (colisDetails.getPoid() != null) {
            if (colisDetails.getPoid().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le poids du colis doit être positif");
            }
            colis.setPoid(colisDetails.getPoid());
        }
        if (colisDetails.getTaille() != null) {
            if (colisDetails.getTaille() <= 0) {
                throw new IllegalArgumentException("La taille du colis doit être positive");
            }
            colis.setTaille(colisDetails.getTaille());
        }
        if (colisDetails.getType() != null) {
            colis.setType(colisDetails.getType());
        }
        return colisRepository.save(colis);
    }

    @Transactional
    public Colis changeDestinataire(Integer id, Integer idNouveauDestinataire) {

        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Colis non trouvé avec l'ID: " + id));

        Utilisateur nouveauDestinataire = utilisateurRepository.findById(idNouveauDestinataire)
                .orElseThrow(() -> new IllegalArgumentException("Destinataire non trouvé avec l'ID: " + idNouveauDestinataire));

        colis.setDestinataire(nouveauDestinataire);
        return colisRepository.save(colis);
    }

    @Transactional
    public void deleteColis(Integer id) {

        if (!colisRepository.existsById(id)) {
            throw new IllegalArgumentException("Colis non trouvé avec l'ID: " + id);
        }
        colisRepository.deleteById(id);
    }
}