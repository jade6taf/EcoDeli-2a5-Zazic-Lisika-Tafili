package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAIEMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Integer idPaiement;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    @Column(name = "montant", precision = 10, scale = 2)
    private BigDecimal montant;

    @PositiveOrZero(message = "La commission ne peut pas être négative")
    @Column(name = "commission", precision = 10, scale = 2)
    private BigDecimal commission;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_de_paiement")
    private ModeDePaiement modeDePaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutPaiement statut = StatutPaiement.PENDING;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_completion")
    private LocalDateTime dateCompletion;

    @ManyToOne
    @JoinColumn(name = "id_annonce")
    private Annonce annonce;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private Client client;

    public enum ModeDePaiement {
        STRIPE, PayPal, Espèces, Virement, Chèque
    }

    public enum StatutPaiement {
        PENDING,     // Paiement en attente
        HELD,        // Fonds bloqués (escrow)
        COMPLETED,   // Paiement complété
        FAILED,      // Échec du paiement
        REFUNDED,    // Remboursé
        CANCELLED    // Annulé
    }
}
