package com.ecodeli.ecodeli_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Integer id;
    private String email;
    private String nom;
    private String prenom;
    private String userType;

    public AuthResponse(String token, Integer id, String email, String nom, String prenom, String userType) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.userType = userType;
        this.type = "Bearer"; // Valeur par défaut
    }
}
