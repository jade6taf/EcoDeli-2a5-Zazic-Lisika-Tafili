package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.exceptions.ResourceNotFoundException;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public List<Utilisateur> getAllUsers() {
        return utilisateurRepository.findAll();
    }

    public List<Utilisateur> getUsersByType(String type) {
        return utilisateurRepository.findAll();
    }

    public Utilisateur getUserById(Integer id) {
        return utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }

    public Utilisateur getUserByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email: " + email));
    }

    public Utilisateur updateUser(Integer id, java.util.Map<String, Object> updateData) {
        Utilisateur user = getUserById(id);

        if (updateData.containsKey("prenom")) {
            user.setPrenom((String) updateData.get("prenom"));
        }
        if (updateData.containsKey("nom")) {
            user.setNom((String) updateData.get("nom"));
        }
        if (updateData.containsKey("email")) {
            String newEmail = (String) updateData.get("email");
            if (!user.getEmail().equals(newEmail) && utilisateurRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Un compte avec cet email existe déjà");
            }
            user.setEmail(newEmail);
        }

        return utilisateurRepository.save(user);
    }

    public void deleteUser(Integer id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
        utilisateurRepository.deleteById(id);
    }

    public Long getUserCountByType(String type) {
        return (long) utilisateurRepository.findAll().size();
    }

    public boolean emailExists(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    public Utilisateur updateUserProfile(Integer userId, java.util.Map<String, Object> profileData) {
        Utilisateur user = getUserById(userId);

        if (profileData.containsKey("prenom")) {
            user.setPrenom((String) profileData.get("prenom"));
        }
        if (profileData.containsKey("nom")) {
            user.setNom((String) profileData.get("nom"));
        }
        if (profileData.containsKey("telephone")) {
            user.setTelephone((String) profileData.get("telephone"));
        }
        if (profileData.containsKey("adresse")) {
            user.setAdresse((String) profileData.get("adresse"));
        }
        if (profileData.containsKey("ville")) {
            user.setVille((String) profileData.get("ville"));
        }
        if (profileData.containsKey("codePostal")) {
            user.setCodePostal((String) profileData.get("codePostal"));
        }
        if (profileData.containsKey("pays")) {
            user.setPays((String) profileData.get("pays"));
        }
        if (profileData.containsKey("genre")) {
            String genreStr = (String) profileData.get("genre");
            if (genreStr != null && !genreStr.isEmpty()) {
                Boolean genreValue = null;
                switch (genreStr.toUpperCase()) {
                    case "HOMME":
                        genreValue = true;
                        break;
                    case "FEMME":
                        genreValue = false;
                        break;
                    default:
                        genreValue = null;
                }
                user.setGenre(genreValue);
            }
        }
        if (profileData.containsKey("dateNaissance")) {
            String dateStr = (String) profileData.get("dateNaissance");
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                    user.setDateDeNaissance(date);
                } catch (Exception e) {
                    // Ignorer si le format de date est incorrect
                }
            }
        }

        return utilisateurRepository.save(user);
    }

}
