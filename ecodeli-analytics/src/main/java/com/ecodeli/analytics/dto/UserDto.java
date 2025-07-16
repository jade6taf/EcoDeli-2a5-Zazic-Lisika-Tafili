package com.ecodeli.analytics.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Data
@Getter
@Setter
public class UserDto {
    private Integer idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
    private String codePostal;
    private String pays;
    private Boolean genre;
    private LocalDate dateDeNaissance;
    private LocalDate dateInscription;
    private String statut;
    private String userType;

    public Integer getId() {
        return idUtilisateur;
    }

    public void setId(Integer id) {
        this.idUtilisateur = id;
    }
}
