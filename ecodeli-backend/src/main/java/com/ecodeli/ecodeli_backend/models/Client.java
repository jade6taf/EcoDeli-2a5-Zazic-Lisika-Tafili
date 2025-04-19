package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CLIENT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Client extends Utilisateur {
    // Vous pouvez ajouter ici des champs spécifiques aux clients
}
