package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.dto.request.LoginRequest;
import com.ecodeli.ecodeli_backend.dto.request.RegisterRequest;
import com.ecodeli.ecodeli_backend.dto.response.AuthResponse;
import com.ecodeli.ecodeli_backend.models.*;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import com.ecodeli.ecodeli_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Utilisateur utilisateur = utilisateurRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!passwordEncoder.matches(loginRequest.getMotDePasse(), utilisateur.getMotDePasse())) {
                throw new RuntimeException("Mot de passe incorrect");
            }

            String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getIdUtilisateur(), getUserType(utilisateur));

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setId(utilisateur.getIdUtilisateur());
            response.setEmail(utilisateur.getEmail());
            response.setPrenom(utilisateur.getPrenom());
            response.setNom(utilisateur.getNom());
            response.setUserType(getUserType(utilisateur));

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la connexion: " + e.getMessage());
        }
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            if (utilisateurRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                throw new RuntimeException("Un compte avec cet email existe déjà");
            }

            Utilisateur utilisateur = createUserByType(registerRequest);

            utilisateur.setMotDePasse(passwordEncoder.encode(registerRequest.getMotDePasse()));

            utilisateur = utilisateurRepository.save(utilisateur);

            emailService.sendWelcomeEmail(utilisateur);

            String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getIdUtilisateur(), getUserType(utilisateur));

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setId(utilisateur.getIdUtilisateur());
            response.setEmail(utilisateur.getEmail());
            response.setPrenom(utilisateur.getPrenom());
            response.setNom(utilisateur.getNom());
            response.setUserType(getUserType(utilisateur));

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    private Utilisateur createUserByType(RegisterRequest request) {
        Utilisateur utilisateur;

        switch (request.getUserType()) {
            case "CLIENT":
                utilisateur = new Client();
                break;
            case "LIVREUR":
                utilisateur = new Livreur();
                break;
            case "PRESTATAIRE":
                Prestataire prestataire = new Prestataire();
                if (request.getNomEntreprise() != null && !request.getNomEntreprise().trim().isEmpty()) {
                    prestataire.setNomEntreprise(request.getNomEntreprise());
                } else {
                    prestataire.setNomEntreprise("Entreprise individuelle");
                }
                prestataire.setSiret(request.getSiret());
                if (request.getDomaineExpertise() != null && !request.getDomaineExpertise().trim().isEmpty()) {
                    try {
                        prestataire.setDomaineExpertise(com.ecodeli.ecodeli_backend.models.ServiceType.valueOf(request.getDomaineExpertise()));
                    } catch (IllegalArgumentException e) {
                        prestataire.setDomaineExpertise(com.ecodeli.ecodeli_backend.models.ServiceType.SERVICES_PERSONNELS);
                    }
                }
                prestataire.setDisponible(true);
                utilisateur = prestataire;
                break;
            case "COMMERCANT":
                utilisateur = new Commercant();
                break;
            case "ADMIN":
                utilisateur = new Admin();
                break;
            default:
                throw new RuntimeException("Type d'utilisateur invalide: " + request.getUserType());
        }

        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(request.getEmail());

        return utilisateur;
    }

    private String getUserType(Utilisateur utilisateur) {
        if (utilisateur instanceof Client) {
            return "CLIENT";
        } else if (utilisateur instanceof Livreur) {
            return "LIVREUR";
        } else if (utilisateur instanceof Prestataire) {
            return "PRESTATAIRE";
        } else if (utilisateur instanceof Commercant) {
            return "COMMERCANT";
        } else if (utilisateur instanceof Admin) {
            return "ADMIN";
        }
        return "UNKNOWN";
    }
}
