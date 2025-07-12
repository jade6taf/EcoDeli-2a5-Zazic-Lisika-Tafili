package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("LIVREUR")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Livreur extends Utilisateur {
    @Pattern(regexp = "^[0-9A-Z]{9}$", message = "Le numéro de permis doit contenir 9 caractères alphanumériques")
    @Column(name = "numero_permis", length = 9)
    private String numeroPermis;

    @Column(name = "vehicule")
    private String vehicule;

    @Column(name = "disponible")
    private Boolean disponible;

    @Column(name = "solde_portefeuille", precision = 10, scale = 2)
    private BigDecimal soldePortefeuille = BigDecimal.ZERO;

    @Column(name = "total_gagnes", precision = 10, scale = 2)
    private BigDecimal totalGagnes = BigDecimal.ZERO;

    @Column(name = "iban_livreur", length = 34)
    private String ibanLivreur;
}
