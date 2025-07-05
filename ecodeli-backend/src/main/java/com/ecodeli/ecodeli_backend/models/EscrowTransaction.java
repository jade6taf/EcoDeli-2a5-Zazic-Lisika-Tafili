package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ESCROW_TRANSACTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscrowTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_escrow")
    private Long idEscrow;

    @NotNull
    @Column(name = "stripe_payment_intent_id", unique = true)
    private String stripePaymentIntentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_annonce", nullable = false)
    private Annonce annonce;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livreur")
    private Livreur livreur;

    @NotNull
    @Positive
    @Column(name = "montant_total", precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @NotNull
    @PositiveOrZero
    @Column(name = "commission_ecodeli", precision = 10, scale = 2)
    private BigDecimal commissionEcodeli;

    @NotNull
    @PositiveOrZero
    @Column(name = "montant_livreur", precision = 10, scale = 2)
    private BigDecimal montantLivreur;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private EscrowStatus statut = EscrowStatus.PENDING;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_liberation")
    private LocalDateTime dateLiberation;

    @Column(name = "segment_numero")
    private Integer segmentNumero; // Pour les livraisons partielles (1 ou 2)

    @Column(name = "notes")
    private String notes;

    public enum EscrowStatus {
        PENDING,        // En attente de paiement
        HELD,           // Fonds bloqués en attente de livraison
        RELEASED,       // Fonds libérés vers le livreur
        REFUNDED,       // Remboursé au client
        FAILED          // Échec de la transaction
    }

    // Méthodes utilitaires
    public void calculerMontants() {
        if (montantTotal != null) {
            // Commission de 10%
            this.commissionEcodeli = montantTotal.multiply(BigDecimal.valueOf(0.10));
            this.montantLivreur = montantTotal.subtract(commissionEcodeli);
        }
    }

    public boolean isLivraisonPartielle() {
        return segmentNumero != null && segmentNumero > 0;
    }
}
