package com.ecodeli.ecodeli_backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "ENTREPOT")
public class Entrepot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrepot")
    private Integer idEntrepot;

    @Column(name = "statut", length = 50)
    private String statut;

    @Column(name = "nombre_de_places")
    private Integer nombreDePlaces;

    @Column(name = "ville", length = 100)
    private String ville;

    public Entrepot() {
    }

    public Integer getIdEntrepot() {
        return idEntrepot;
    }

    public void setIdEntrepot(Integer idEntrepot) {
        this.idEntrepot = idEntrepot;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getNombreDePlaces() {
        return nombreDePlaces;
    }

    public void setNombreDePlaces(Integer nombreDePlaces) {
        this.nombreDePlaces = nombreDePlaces;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }
}