package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CONTRAT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat")
    private Integer idContrat;

    @ManyToOne
    @JoinColumn(name = "id_commercant", nullable = false)
    private Commercant commercant;

    @ManyToOne
    @JoinColumn(name = "id_admin", nullable = false)
    private Admin admin;

    @Column(name = "numero_contrat", length = 50, unique = true)
    private String numeroContrat;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutContrat statut = StatutContrat.BROUILLON;

    @NotNull(message = "La commission est obligatoire")
    @DecimalMin(value = "0.0", message = "La commission doit être positive")
    @DecimalMax(value = "100.0", message = "La commission ne peut pas dépasser 100%")
    @Column(name = "commission_pourcentage", precision = 5, scale = 2)
    private BigDecimal commissionPourcentage;

    @DecimalMin(value = "0.0", message = "Les frais fixes doivent être positifs")
    @Column(name = "frais_fixe_mensuel", precision = 10, scale = 2)
    private BigDecimal fraisFixeMensuel = BigDecimal.ZERO;

    @Column(name = "conditions_service", columnDefinition = "TEXT")
    private String conditionsService;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "date_signature_commercant")
    private LocalDateTime dateSignatureCommercant;

    @Column(name = "date_signature_admin")
    private LocalDateTime dateSignatureAdmin;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_modification")
    private LocalDateTime dateModification = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (dateModification == null) {
            dateModification = LocalDateTime.now();
        }
    }

    public boolean isSigneParCommercant() {
        return dateSignatureCommercant != null;
    }

    public boolean isSigneParAdmin() {
        return dateSignatureAdmin != null;
    }

    public boolean isActif() {
        return statut == StatutContrat.ACTIF;
    }

    public boolean peutEtreModifie() {
        return statut == StatutContrat.BROUILLON;
    }

    public boolean peutEtreEnvoyePourSignature() {
        return statut == StatutContrat.BROUILLON && commercant != null && commissionPourcentage != null;
    }

    public boolean peutEtreSigneParCommercant() {
        return statut == StatutContrat.ATTENTE_SIGNATURE_COMMERCANT;
    }

    public boolean peutEtreValideParAdmin() {
        return statut == StatutContrat.ATTENTE_SIGNATURE_ADMIN;
    }
}
