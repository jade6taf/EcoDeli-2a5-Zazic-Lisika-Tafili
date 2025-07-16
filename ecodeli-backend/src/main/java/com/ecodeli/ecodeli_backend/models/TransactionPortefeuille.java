package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TRANSACTION_PORTEFEUILLE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPortefeuille {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private Long idTransaction;
    
    @ManyToOne
    @JoinColumn(name = "id_portefeuille", nullable = false)
    private PortefeuillePrestataire portefeuille;
    
    @ManyToOne
    @JoinColumn(name = "id_candidature")
    private Candidature candidature;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_transaction", nullable = false)
    private TypeTransaction typeTransaction;
    
    @Column(name = "montant", precision = 10, scale = 2, nullable = false)
    private BigDecimal montant;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_transaction", nullable = false)
    private StatutTransaction statutTransaction;
    
    @Column(name = "reference_externe", length = 100)
    private String referenceExterne;
    
    @Column(name = "iban_destinataire", length = 34)
    private String ibanDestinataire;
    
    @Column(name = "frais_transaction", precision = 10, scale = 2)
    private BigDecimal fraisTransaction = BigDecimal.ZERO;
    
    @Column(name = "commission_ecodeli", precision = 10, scale = 2)
    private BigDecimal commissionEcodeli = BigDecimal.ZERO;
    
    @Column(name = "date_transaction", nullable = false)
    private LocalDateTime dateTransaction;
    
    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (dateTransaction == null) {
            dateTransaction = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    public enum TypeTransaction {
        CREDIT_MISSION("Crédit mission"),
        RETRAIT_PRESTATAIRE("Retrait prestataire"),
        FRAIS_SERVICE("Frais de service"),
        BONUS("Bonus"),
        REMBOURSEMENT("Remboursement"),
        CORRECTION("Correction");
        
        private final String label;
        
        TypeTransaction(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public enum StatutTransaction {
        EN_ATTENTE("En attente"),
        EN_COURS("En cours de traitement"),
        EFFECTUE("Effectuée"),
        ECHOUE("Échec"),
        ANNULE("Annulée");
        
        private final String label;
        
        StatutTransaction(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public void marquerEffectuee() {
        this.statutTransaction = StatutTransaction.EFFECTUE;
        this.dateTraitement = LocalDateTime.now();
    }
    
    public void marquerEchouee() {
        this.statutTransaction = StatutTransaction.ECHOUE;
        this.dateTraitement = LocalDateTime.now();
    }
    
    public BigDecimal getMontantNet() {
        return montant.subtract(fraisTransaction != null ? fraisTransaction : BigDecimal.ZERO);
    }
    
    public boolean isCredit() {
        return typeTransaction == TypeTransaction.CREDIT_MISSION ||
               typeTransaction == TypeTransaction.BONUS ||
               typeTransaction == TypeTransaction.REMBOURSEMENT;
    }
    
    public boolean isDebit() {
        return typeTransaction == TypeTransaction.RETRAIT_PRESTATAIRE ||
               typeTransaction == TypeTransaction.FRAIS_SERVICE;
    }
    
    public String getDescriptionComplete() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        
        StringBuilder desc = new StringBuilder();
        desc.append(typeTransaction.getLabel());
        
        if (candidature != null && candidature.getDemandeService() != null) {
            desc.append(" - ").append(candidature.getDemandeService().getTitre());
        }
        
        if (montant != null) {
            String signe = isCredit() ? "+" : "-";
            desc.append(" (").append(signe).append(montant).append("€)");
        }
        
        return desc.toString();
    }
    
    public static String genererReference(TypeTransaction type, Long prestataireId) {
        String prefix = type == TypeTransaction.CREDIT_MISSION ? "CM" :
                       type == TypeTransaction.RETRAIT_PRESTATAIRE ? "RP" : "TX";
        long timestamp = System.currentTimeMillis();
        return String.format("%s_%d_%d", prefix, prestataireId, timestamp);
    }
}