package com.ecodeli.ecodeli_backend.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoursesDetailsDTO {
    @NotBlank(message = "Le type d'achat est obligatoire")
    @Pattern(regexp = "^(alimentaire|pharmacie|vetements|electromenager|divers)$",
             message = "Type d'achat invalide")
    private String typeAchat;

    @NotBlank(message = "La liste des articles est obligatoire")
    @Size(min = 5, max = 2000, message = "La liste doit faire entre 5 et 2000 caractères")
    private String listeArticles;

    @NotBlank(message = "L'adresse de livraison est obligatoire")
    private String adresseLivraison;

    @NotBlank(message = "La date de livraison est obligatoire")
    private String dateLivraison;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le budget doit être positif")
    private BigDecimal budgetEstimatif;

    private String magasinsPreferences;
    private String instructions;
    private String creneauLivraison;

    private Boolean produitsBio;
    private Boolean substitutsAcceptes;
    private Boolean livraison24h;

    private String modeReception; // domicile, point_relais, consigne
    private String codeAcces;
    private String etage;
    private String interphone;

    private String moyenPaiement; // carte, especes, cheque
    private Boolean ticketDeCaisse;
    private String contraintesAllergies;
}
