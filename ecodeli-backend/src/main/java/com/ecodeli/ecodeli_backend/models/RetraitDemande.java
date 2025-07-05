package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RETRAIT_DEMANDE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetraitDemande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_retrait")
    private Long idRetrait;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livreur", nullable = false)
    private Livreur livreur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_wallet", nullable = false)
    private WalletLivreur wallet;

    @NotNull
    @Positive
    @DecimalMin(value = "1.0", message = "Le montant minimum de retrait est de 1€")
    @Column(name = "montant", precision = 10, scale = 2)
    private BigDecimal montant;

    @NotBlank
    @Column(name = "iban", nullable = false)
    private String iban;

    @NotBlank
    @Column(name = "nom_titulaire", nullable = false)
    private String nomTitulaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutRetrait statut = StatutRetrait.PENDING;

    @Column(name = "date_demande")
    private LocalDateTime dateDemande = LocalDateTime.now();

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    @Column(name = "reference_virement")
    private String referenceVirement; // Référence Stripe ou bancaire

    @Column(name = "motif_refus")
    private String motifRefus;

    @Column(name = "frais_appliques", precision = 10, scale = 2)
    private BigDecimal fraisAppliques = BigDecimal.ZERO; // Frais éventuels (actuellement 0)

    @Column(name = "montant_net", precision = 10, scale = 2)
    private BigDecimal montantNet; // Montant après déduction des frais

    @Column(name = "is_fictive")
    private Boolean isFictive = true; // Par défaut fictif

    @Enumerated(EnumType.STRING)
    @Column(name = "type_retrait")
    private TypeRetrait typeRetrait = TypeRetrait.FICTIF;

    public enum StatutRetrait {
        PENDING,        // En attente de traitement
        PROCESSING,     // En cours de traitement
        COMPLETED,      // Virement effectué
        FAILED,         // Échec du virement
        CANCELLED       // Annulé par le livreur ou admin
    }

    public enum TypeRetrait {
        FICTIF,         // Déduction wallet uniquement
        REEL_SEPA       // Vrai virement bancaire (futur)
    }

    @PrePersist
    @PreUpdate
    public void calculerMontantNet() {
        if (montant != null && fraisAppliques != null) {
            this.montantNet = montant.subtract(fraisAppliques);
        }
    }

    public boolean isTraitable() {
        return statut == StatutRetrait.PENDING && 
               montant.compareTo(BigDecimal.valueOf(1)) >= 0 &&
               iban != null && !iban.trim().isEmpty() &&
               nomTitulaire != null && !nomTitulaire.trim().isEmpty();
    }

    public void marquerCommeTraite(LocalDateTime dateTraitement, String referenceVirement) {
        this.statut = StatutRetrait.COMPLETED;
        this.dateTraitement = dateTraitement;
        this.referenceVirement = referenceVirement;
    }

    public void marquerCommeEchec(String motifRefus) {
        this.statut = StatutRetrait.FAILED;
        this.dateTraitement = LocalDateTime.now();
        this.motifRefus = motifRefus;
    }
}
