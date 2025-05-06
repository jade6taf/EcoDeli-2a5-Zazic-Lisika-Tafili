package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> getUtilisateurById(Integer id) {
        return utilisateurRepository.findById(id);
    }

    public Optional<Utilisateur> getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }
        String type = utilisateur.getType();
        if ("ADMIN".equals(type) && !(utilisateur instanceof com.ecodeli.ecodeli_backend.models.Admin)) {
            com.ecodeli.ecodeli_backend.models.Admin admin = new com.ecodeli.ecodeli_backend.models.Admin();
            admin.setNom(utilisateur.getNom());
            admin.setPrenom(utilisateur.getPrenom());
            admin.setGenre(utilisateur.getGenre());
            admin.setDateDeNaissance(utilisateur.getDateDeNaissance());
            admin.setEmail(utilisateur.getEmail());
            admin.setMotDePasse(utilisateur.getMotDePasse());
            admin.setTelephone(utilisateur.getTelephone());
            admin.setAdresse(utilisateur.getAdresse());
            admin.setVille(utilisateur.getVille());
            admin.setCodePostal(utilisateur.getCodePostal());
            admin.setPays(utilisateur.getPays());
            return utilisateurRepository.save(admin);
        }
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur updateUtilisateur(Integer id, Utilisateur utilisateurDetails) {
        Optional<Utilisateur> optionalUtilisateur = utilisateurRepository.findById(id);

        if (!optionalUtilisateur.isPresent()) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'id: " + id);
        }

        Utilisateur utilisateur = optionalUtilisateur.get();

        utilisateur.setNom(utilisateurDetails.getNom());
        utilisateur.setPrenom(utilisateurDetails.getPrenom());
        utilisateur.setGenre(utilisateurDetails.getGenre());
        utilisateur.setDateDeNaissance(utilisateurDetails.getDateDeNaissance());

        if (!utilisateur.getEmail().equals(utilisateurDetails.getEmail())) {
            if (utilisateurRepository.existsByEmail(utilisateurDetails.getEmail())) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre utilisateur");
            }
            utilisateur.setEmail(utilisateurDetails.getEmail());
        }

        if (utilisateurDetails.getMotDePasse() != null && !utilisateurDetails.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasse(utilisateurDetails.getMotDePasse());
        }

        utilisateur.setTelephone(utilisateurDetails.getTelephone());
        utilisateur.setAdresse(utilisateurDetails.getAdresse());
        utilisateur.setVille(utilisateurDetails.getVille());
        utilisateur.setCodePostal(utilisateurDetails.getCodePostal());
        utilisateur.setPays(utilisateurDetails.getPays());
        return utilisateurRepository.save(utilisateur);
    }

    public void deleteUtilisateur(Integer id) {
        utilisateurRepository.deleteById(id);
    }

    public List<Utilisateur> getUtilisateursByType(String type) {
        return utilisateurRepository.findByType(type);
    }

}
