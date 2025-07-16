package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PORTEFEUILLE_PRESTATAIRE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortefeuillePrestataire {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_portefeuille")
    private Long idPortefeuille;
    
    @OneToOne
    @JoinColumn(name = "id_prestataire", nullable = false, unique = true)
    private Prestataire prestataire;
    
    @Column(name = "solde_disponible", precision = 10, scale = 2, nullable = false)
    private BigDecimal soldeDisponible = BigDecimal.ZERO;
    
    @Column(name = "solde_en_attente", precision = 10, scale = 2, nullable = false)
    private BigDecimal soldeEnAttente = BigDecimal.ZERO;
    
    @Column(name = "total_gagne", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalGagne = BigDecimal.ZERO;
    
    @Column(name = "total_retire", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalRetire = BigDecimal.ZERO;
    
    @Column(name = "nombre_transactions", nullable = false)
    private Integer nombreTransactions = 0;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @Column(name = "iban_prestataire", length = 34)
    private String ibanPrestataire;
    
    @Column(name = "nom_titulaire", length = 100)
    private String nomTitulaire;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
    
    /**
     * Crédite le portefeuille avec le montant d'une mission
     */
    public void crediterMission(BigDecimal montant) {
        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            this.soldeDisponible = this.soldeDisponible.add(montant);
            this.totalGagne = this.totalGagne.add(montant);
            this.nombreTransactions++;
        }
    }
    
    /**
     * Débite le portefeuille pour un retrait
     */
    public boolean debiterRetrait(BigDecimal montant) {
        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0 
            && this.soldeDisponible.compareTo(montant) >= 0) {
            this.soldeDisponible = this.soldeDisponible.subtract(montant);
            this.totalRetire = this.totalRetire.add(montant);
            this.nombreTransactions++;
            return true;
        }
        return false;
    }
    
    /**
     * Vérifie si le retrait est possible
     */
    public boolean peutRetirer(BigDecimal montant) {
        return montant != null && montant.compareTo(BigDecimal.ZERO) > 0 
               && this.soldeDisponible.compareTo(montant) >= 0;
    }
    
    /**
     * Calcule le solde total (disponible + en attente)
     */
    public BigDecimal getSoldeTotal() {
        return this.soldeDisponible.add(this.soldeEnAttente);
    }
    
    /**
     * Met à jour les informations bancaires
     */
    public void updateInfosBancaires(String iban, String nomTitulaire) {
        this.ibanPrestataire = iban;
        this.nomTitulaire = nomTitulaire;
    }
    
    /**
     * Retourne l'IBAN masqué pour l'affichage
     */
    public String getIbanMasque() {
        if (ibanPrestataire == null || ibanPrestataire.length() < 8) {
            return "Aucun IBAN renseigné";
        }
        return ibanPrestataire.substring(0, 4) + " ****" + 
               ibanPrestataire.substring(ibanPrestataire.length() - 4);
    }
}