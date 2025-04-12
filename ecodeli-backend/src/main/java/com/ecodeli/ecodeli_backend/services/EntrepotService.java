package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Entrepot;
import com.ecodeli.ecodeli_backend.models.Entrepot.StatutEntrepot;
import com.ecodeli.ecodeli_backend.repositories.EntrepotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EntrepotService {

    private final EntrepotRepository entrepotRepository;

    public EntrepotService(EntrepotRepository entrepotRepository) {
        this.entrepotRepository = entrepotRepository;
    }

    public List<Entrepot> getAllEntrepots() {
        return entrepotRepository.findAll();
    }

    public Optional<Entrepot> getEntrepotById(Integer id) {
        return entrepotRepository.findById(id);
    }

    public List<Entrepot> getEntrepotsByStatut(StatutEntrepot statut) {
        return entrepotRepository.findByStatut(statut);
    }

    public List<Entrepot> getEntrepotsByVille(String ville) {
        return entrepotRepository.findByVille(ville);
    }

    public List<Entrepot> getActiveEntrepotsByVille(String ville) {
        return entrepotRepository.findActiveEntrepotsByCity(ville);
    }

    public List<Entrepot> searchEntrepotsByPlacesRange(Integer min, Integer max) {
        return entrepotRepository.findByPlacesRange(min, max);
    }

    public Integer calculateTotalAvailablePlaces() {
        return entrepotRepository.calculateTotalAvailablePlaces();
    }

    @Transactional
    public Entrepot createEntrepot(Entrepot entrepot) {
        if (entrepot.getNombreDePlaces() == null || entrepot.getNombreDePlaces() <= 0) {
            throw new IllegalArgumentException("Le nombre de places doit être positif");
        }
        if (entrepot.getVille() == null || entrepot.getVille().trim().isEmpty()) {
            throw new IllegalArgumentException("La ville est obligatoire");
        }
        if (entrepot.getStatut() == null) {
            entrepot.setStatut(StatutEntrepot.ACTIF);
        }
        return entrepotRepository.save(entrepot);
    }

    @Transactional
    public Entrepot updateEntrepot(Integer id, Entrepot entrepotDetails) {

        Entrepot entrepot = entrepotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrepôt non trouvé avec l'ID: " + id));

        if (entrepotDetails.getNombreDePlaces() != null) {
            if (entrepotDetails.getNombreDePlaces() <= 0) {
                throw new IllegalArgumentException("Le nombre de places doit être positif");
            }
            entrepot.setNombreDePlaces(entrepotDetails.getNombreDePlaces());
        }
        if (entrepotDetails.getVille() != null) {
            if (entrepotDetails.getVille().trim().isEmpty()) {
                throw new IllegalArgumentException("La ville ne peut pas être vide");
            }
            entrepot.setVille(entrepotDetails.getVille());
        }
        if (entrepotDetails.getStatut() != null) {
            entrepot.setStatut(entrepotDetails.getStatut());
        }
        return entrepotRepository.save(entrepot);
    }

    @Transactional
    public Entrepot changeStatut(Integer id, StatutEntrepot nouveauStatut) {
        Entrepot entrepot = entrepotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrepôt non trouvé avec l'ID: " + id));

        entrepot.setStatut(nouveauStatut);
        return entrepotRepository.save(entrepot);
    }

    @Transactional
    public Entrepot augmenterCapacite(Integer id, Integer placesSupplementaires) {
        if (placesSupplementaires <= 0) {
            throw new IllegalArgumentException("Le nombre de places supplémentaires doit être positif");
        }
        Entrepot entrepot = entrepotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrepôt non trouvé avec l'ID: " + id));

        entrepot.setNombreDePlaces(entrepot.getNombreDePlaces() + placesSupplementaires);
        return entrepotRepository.save(entrepot);
    }

    @Transactional
    public Entrepot diminuerCapacite(Integer id, Integer placesARetirer) {
        if (placesARetirer <= 0) {
            throw new IllegalArgumentException("Le nombre de places à retirer doit être positif");
        }

        Entrepot entrepot = entrepotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entrepôt non trouvé avec l'ID: " + id));

        if (entrepot.getNombreDePlaces() < placesARetirer) {
            throw new IllegalArgumentException("Le nombre de places à retirer ne peut pas excéder le nombre de places actuelles");
        }
        entrepot.setNombreDePlaces(entrepot.getNombreDePlaces() - placesARetirer);
        return entrepotRepository.save(entrepot);
    }

    @Transactional
    public void deleteEntrepot(Integer id) {
        if (!entrepotRepository.existsById(id)) {
            throw new IllegalArgumentException("Entrepôt non trouvé avec l'ID: " + id);
        }
        entrepotRepository.deleteById(id);
    }
}