package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

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

    public enum ModeDePaiement {
        CB, PayPal, Espèces, Virement, Chèque
    }
}