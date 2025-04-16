package com.ecodeli.ecodeli_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecodeli.ecodeli_backend.models.AuthRequest;
import com.ecodeli.ecodeli_backend.models.JwtResponse;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.security.JwtUtil;
import com.ecodeli.ecodeli_backend.services.UtilisateurService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UtilisateurService utilisateurService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.utilisateurService = utilisateurService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Utilisateur utilisateur) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));

        try {
            Utilisateur newUser = utilisateurService.createUtilisateur(utilisateur);
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
                // Générer un token JWT pour l'utilisateur authentifié
                String token = jwtUtil.generateToken(user.getEmail(), user.getIdUtilisateur(), user.getType());
                return ResponseEntity.ok(new JwtResponse(token, user));
            } else {
                return ResponseEntity.badRequest().body("Mot de passe incorrect");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Email ou mot de passe incorrect");
        }
    }
}