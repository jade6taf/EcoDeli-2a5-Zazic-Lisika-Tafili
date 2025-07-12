package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RETRAIT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Retrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_retrait")
    private Integer idRetrait;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    @Column(name = "montant", precision = 10, scale = 2)
    private BigDecimal montant;

    @NotBlank(message = "L'IBAN est obligatoire")
    @Column(name = "iban", length = 34)
    private String iban;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutRetrait statut = StatutRetrait.EN_ATTENTE;

    @Column(name = "date_demande")
    private LocalDateTime dateDemande = LocalDateTime.now();

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    @ManyToOne
    @JoinColumn(name = "id_livreur", nullable = false)
    private Livreur livreur;

    public enum StatutRetrait {
        EN_ATTENTE,     // Demande de retrait en attente
        TRAITE,         // Retrait effectué (fictif)
        REFUSE          // Retrait refusé (solde insuffisant)
    }
}
