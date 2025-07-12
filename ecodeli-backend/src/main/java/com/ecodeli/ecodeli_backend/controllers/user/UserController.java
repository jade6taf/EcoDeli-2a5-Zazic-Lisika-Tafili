package com.ecodeli.ecodeli_backend.controllers.user;

import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> profileData) {
        try {
            Integer userId = (Integer) profileData.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID utilisateur requis"));
            }

            Utilisateur updatedUser = userService.updateUserProfile(userId, profileData);

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("idUtilisateur", updatedUser.getIdUtilisateur());
            userProfile.put("nom", updatedUser.getNom());
            userProfile.put("prenom", updatedUser.getPrenom());
            userProfile.put("email", updatedUser.getEmail());
            userProfile.put("telephone", updatedUser.getTelephone());
            userProfile.put("adresse", updatedUser.getAdresse());
            userProfile.put("ville", updatedUser.getVille());
            userProfile.put("codePostal", updatedUser.getCodePostal());
            userProfile.put("pays", updatedUser.getPays());
            Boolean genreBoolean = updatedUser.getGenre();
            String genreString = null;
            if (genreBoolean != null) {
                if (genreBoolean) {
                    genreString = "HOMME";
                } else {
                    genreString = "FEMME";
                }
            }
            userProfile.put("genre", genreString);
            userProfile.put("dateDeNaissance", updatedUser.getDateDeNaissance());
            userProfile.put("type", updatedUser.getType());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profil mis à jour avec succès",
                "user", userProfile
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "Erreur lors de la mise à jour du profil: " + e.getMessage()
            ));
        }
    }


    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Integer userId) {
        try {
            Utilisateur user = userService.getUserById(userId);

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("idUtilisateur", user.getIdUtilisateur());
            userProfile.put("nom", user.getNom());
            userProfile.put("prenom", user.getPrenom());
            userProfile.put("email", user.getEmail());
            userProfile.put("telephone", user.getTelephone());
            userProfile.put("adresse", user.getAdresse());
            userProfile.put("ville", user.getVille());
            userProfile.put("codePostal", user.getCodePostal());
            userProfile.put("pays", user.getPays());
            Boolean genreBoolean = user.getGenre();
            String genreString = null;
            if (genreBoolean != null) {
                if (genreBoolean) {
                    genreString = "HOMME";
                } else {
                    genreString = "FEMME";
                }
            }
            userProfile.put("genre", genreString);
            userProfile.put("dateDeNaissance", user.getDateDeNaissance());
            userProfile.put("type", user.getType());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", userProfile
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", "Utilisateur non trouvé"
            ));
        }
    }
}
