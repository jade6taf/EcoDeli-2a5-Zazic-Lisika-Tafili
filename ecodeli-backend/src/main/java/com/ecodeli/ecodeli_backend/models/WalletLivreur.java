package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "WALLET_LIVREUR")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletLivreur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wallet")
    private Long idWallet;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_livreur", nullable = false, unique = true)
    private Livreur livreur;

    @NotNull
    @PositiveOrZero
    @Column(name = "solde_disponible", precision = 10, scale = 2)
    private BigDecimal soldeDisponible = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "solde_en_attente", precision = 10, scale = 2)
    private BigDecimal soldeEnAttente = BigDecimal.ZERO;

    @Column(name = "iban")
    private String iban;

    @Column(name = "nom_titulaire_compte")
    private String nomTitulaireCompte;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_derniere_maj")
    private LocalDateTime dateDerniereMaj = LocalDateTime.now();

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionWallet> transactions = new ArrayList<>();

    // Méthodes utilitaires
    public void ajouterFonds(BigDecimal montant, String description, TransactionWallet.TypeTransaction type) {
        this.soldeDisponible = this.soldeDisponible.add(montant);
        this.dateDerniereMaj = LocalDateTime.now();
        
        TransactionWallet transaction = new TransactionWallet();
        transaction.setWallet(this);
        transaction.setMontant(montant);
        transaction.setDescription(description);
        transaction.setType(type);
        transaction.setDateTransaction(LocalDateTime.now());
        this.transactions.add(transaction);
    }

    public void debiterFonds(BigDecimal montant, String description, TransactionWallet.TypeTransaction type) {
        if (this.soldeDisponible.compareTo(montant) >= 0) {
            this.soldeDisponible = this.soldeDisponible.subtract(montant);
            this.dateDerniereMaj = LocalDateTime.now();
            
            TransactionWallet transaction = new TransactionWallet();
            transaction.setWallet(this);
            transaction.setMontant(montant.negate()); // Montant négatif pour débit
            transaction.setDescription(description);
            transaction.setType(type);
            transaction.setDateTransaction(LocalDateTime.now());
            this.transactions.add(transaction);
        } else {
            throw new IllegalArgumentException("Solde insuffisant pour effectuer cette opération");
        }
    }

    public BigDecimal getSoldeTotal() {
        return soldeDisponible.add(soldeEnAttente);
    }

    public boolean peutRetirerMontant(BigDecimal montant) {
        return montant.compareTo(BigDecimal.valueOf(10)) >= 0 && 
               soldeDisponible.compareTo(montant) >= 0;
    }

    public boolean hasValidIban() {
        return iban != null && !iban.trim().isEmpty() && 
               nomTitulaireCompte != null && !nomTitulaireCompte.trim().isEmpty();
    }
}
