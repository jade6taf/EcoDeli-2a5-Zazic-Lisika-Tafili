package com.ecodeli.ecodeli_backend.controllers.admin;

import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(
            @RequestParam(required = false) String type) {

        List<Utilisateur> users;
        if (type != null && !type.isEmpty()) {
            users = userService.getUsersByType(type);
        } else {
            users = userService.getAllUsers();
        }

        List<Map<String, Object>> enrichedUsers = users.stream()
            .map(this::enrichUserData)
            .collect(Collectors.toList());

        return ResponseEntity.ok(enrichedUsers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getUserById(@PathVariable Integer id) {
        Utilisateur user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> updateData) {
        Utilisateur updatedUser = userService.updateUser(id, updateData);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur supprimé avec succès");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userService.getAllUsers().size());
        stats.put("clients", userService.getUserCountByType("CLIENT"));
        stats.put("livreurs", userService.getUserCountByType("LIVREUR"));
        stats.put("commercants", userService.getUserCountByType("COMMERCANT"));
        stats.put("prestataires", userService.getUserCountByType("PRESTATAIRE"));
        stats.put("admins", userService.getUserCountByType("ADMIN"));

        return ResponseEntity.ok(stats);
    }

    private Map<String, Object> enrichUserData(Utilisateur user) {
        Map<String, Object> userData = new HashMap<>();

        userData.put("idUtilisateur", user.getIdUtilisateur());
        userData.put("nom", user.getNom());
        userData.put("prenom", user.getPrenom());
        userData.put("email", user.getEmail());
        userData.put("telephone", user.getTelephone());
        userData.put("adresse", user.getAdresse());
        userData.put("ville", user.getVille());
        userData.put("codePostal", user.getCodePostal());
        userData.put("pays", user.getPays());
        userData.put("genre", user.getGenre());
        userData.put("dateDeNaissance", user.getDateDeNaissance());

        userData.put("userType", user.getType());

        return userData;
    }
}
