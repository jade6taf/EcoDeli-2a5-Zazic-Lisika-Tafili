package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRANSACTION_WALLET")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Long idTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_wallet", nullable = false)
    private WalletLivreur wallet;

    @NotNull
    @Column(name = "montant", precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TypeTransaction type;

    @Column(name = "description")
    private String description;

    @Column(name = "date_transaction")
    private LocalDateTime dateTransaction = LocalDateTime.now();

    @Column(name = "reference_externe")
    private String referenceExterne; // ID Stripe, référence virement, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livraison")
    private Livraison livraison; // Référence à la livraison si applicable

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutTransaction statut = StatutTransaction.COMPLETED;

    public enum TypeTransaction {
        CREDIT_LIVRAISON,    // Crédit suite à une livraison
        DEBIT_RETRAIT,       // Débit pour un retrait réel
        DEBIT_RETRAIT_FICTIF, // Débit pour un retrait fictif
        CREDIT_BONUS,        // Crédit bonus/promotion
        DEBIT_FRAIS,         // Débit pour frais
        CREDIT_CORRECTION,   // Crédit de correction
        DEBIT_CORRECTION     // Débit de correction
    }

    public enum StatutTransaction {
        PENDING,     // En attente
        COMPLETED,   // Complétée
        FAILED,      // Échouée
        CANCELLED    // Annulée
    }

    public boolean isCredit() {
        return montant.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isDebit() {
        return montant.compareTo(BigDecimal.ZERO) < 0;
    }
}
