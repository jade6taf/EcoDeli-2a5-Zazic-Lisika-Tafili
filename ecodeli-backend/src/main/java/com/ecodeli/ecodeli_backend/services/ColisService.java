package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Colis;
import com.ecodeli.ecodeli_backend.repositories.ColisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ColisService {

    private final ColisRepository colisRepository;

    public ColisService(ColisRepository colisRepository) {
        this.colisRepository = colisRepository;
    }

    public List<Colis> getAllColis() {
        return colisRepository.findAll();
    }

    public Optional<Colis> getColisById(Integer id) {
        return colisRepository.findById(id);
    }

    @Transactional
    public Colis createColis(Colis colis) {
        return colisRepository.save(colis);
    }

    @Transactional
    public Colis updateColis(Integer id, Colis colisDetails) {
        Colis colis = colisRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Colis non trouvé avec l'ID: " + id));

        if (colisDetails.getPoids() != null)
            colis.setPoids(colisDetails.getPoids());
        if (colisDetails.getLongueur() != null)
            colis.setLongueur(colisDetails.getLongueur());
        if (colisDetails.getLargeur() != null)
            colis.setLargeur(colisDetails.getLargeur());
        if (colisDetails.getHauteur() != null)
            colis.setHauteur(colisDetails.getHauteur());
        if (colisDetails.getFragile() != null)
            colis.setFragile(colisDetails.getFragile());
        if (colisDetails.getDescription() != null)
            colis.setDescription(colisDetails.getDescription());
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