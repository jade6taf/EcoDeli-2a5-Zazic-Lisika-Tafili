package com.ecodeli.ecodeli_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecodeli.ecodeli_backend.models.AuthRequest;
import com.ecodeli.ecodeli_backend.models.JwtResponse;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.security.JwtUtil;
import com.ecodeli.ecodeli_backend.services.UtilisateurService;
import com.ecodeli.ecodeli_backend.services.PasswordSecurityService;
import com.ecodeli.ecodeli_backend.services.EmailService;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PasswordSecurityService passwordSecurityService;
    private final EmailService emailService;

    public AuthController(UtilisateurService utilisateurService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, PasswordSecurityService passwordSecurityService, EmailService emailService) {
        this.utilisateurService = utilisateurService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.passwordSecurityService = passwordSecurityService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Utilisateur utilisateur) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));

        try {
            Utilisateur newUser = utilisateurService.createUtilisateur(utilisateur);

            try {
                emailService.sendWelcomeEmail(newUser);
            } catch (Exception emailException) {
                System.err.println("Erreur lors de l'envoi de l'email de bienvenue : " + emailException.getMessage());
            }
            String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getIdUtilisateur(), newUser.getType());
            return ResponseEntity.ok(new JwtResponse(token, newUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("L'email existe déjà");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            Utilisateur user = utilisateurService.getUtilisateurByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (passwordEncoder.matches(request.getPassword(), user.getMotDePasse())) {
                String token = jwtUtil.generateToken(user.getEmail(), user.getIdUtilisateur(), user.getType());
                return ResponseEntity.ok(new JwtResponse(token, user));
            } else {
                return ResponseEntity.badRequest().body("Mot de passe incorrect");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Email ou mot de passe incorrect");
        }
    }

    @PostMapping("/validate-password")
    public ResponseEntity<?> validatePassword(@RequestBody PasswordValidationRequest request) {
        try {
            Utilisateur tempUser = new com.ecodeli.ecodeli_backend.models.Client();
            tempUser.setNom(request.getNom());
            tempUser.setPrenom(request.getPrenom());
            tempUser.setEmail(request.getEmail());
            PasswordSecurityService.PasswordValidationResult result =
                passwordSecurityService.validatePassword(request.getPassword(), tempUser);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la validation du mot de passe");
        }
    }

    @GetMapping("/generate-password")
    public ResponseEntity<?> generatePassword(@RequestParam(defaultValue = "12") int length) {
        try {
            String password = passwordSecurityService.generateSecurePassword(length);
            int strength = passwordSecurityService.calculatePasswordStrength(password);
            return ResponseEntity.ok(new GeneratedPasswordResponse(password, strength));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la génération du mot de passe");
        }
    }

    public static class PasswordValidationRequest {
        private String password;
        private String nom;
        private String prenom;
        private String email;

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class GeneratedPasswordResponse {
        private String password;
        private int strength;

        public GeneratedPasswordResponse(String password, int strength) {
            this.password = password;
            this.strength = strength;
        }

        public String getPassword() { return password; }
        public int getStrength() { return strength; }
    }
}
