package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "FACTURE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_facture")
    private Integer idFacture;

    @Column(name = "prestations", columnDefinition = "TEXT")
    private String prestations;

    @NotNull(message = "Le prix total est obligatoire")
    @Positive(message = "Le prix total doit être positif")
    @Column(name = "prix_total", precision = 10, scale = 2)
    private BigDecimal prixTotal;

    @Column(name = "date_facture")
    private LocalDateTime dateFacture;

    @Column(name = "statut", length = 50)
    private String statut;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_de_paiement")
    private ModeDePaiement modeDePaiement;

    @Min(value = 0, message = "La TVA ne peut pas être négative")
    @Max(value = 100, message = "La TVA ne peut pas dépasser 100%")
    @Column(name = "tva")
    private Integer tva;

    @Min(value = 0, message = "La remise ne peut pas être négative")
    @Max(value = 100, message = "La remise ne peut pas dépasser 100%")
    @Column(name = "remise")
    private Integer remise;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @ManyToOne
    @JoinColumn(name = "id_produit")
    private Produit produit;

    @ManyToOne
    @JoinColumn(name = "id_prestataire", nullable = false)
    private Utilisateur prestataire;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Utilisateur client;

    public enum ModeDePaiement {
        CB, PayPal, Espèces, Virement, Chèque
    }
}