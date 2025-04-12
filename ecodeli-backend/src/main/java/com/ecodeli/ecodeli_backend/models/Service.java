package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SERVICE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Integer idService;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    @Column(name = "prix_unitaire", precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "adresse_de_livraison", length = 100)
    private String adresseDeLivraison;

    @Column(name = "code_postal_livraison", length = 10)
    private String codePostalLivraison;

    @Column(name = "adresse_envoi", length = 100)
    private String adresseEnvoi;

    @Column(name = "code_postal_envoi", length = 10)
    private String codePostalEnvoi;

    @Column(name = "type_service", length = 100)
    private String typeService;

    @ManyToOne
    @JoinColumn(name = "id_prestataire", nullable = false)
    private Utilisateur prestataire;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Utilisateur client;
}