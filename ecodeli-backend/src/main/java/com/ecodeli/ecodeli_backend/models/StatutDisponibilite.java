package com.ecodeli.ecodeli_backend.models;

public enum StatutDisponibilite {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    TEMPORAIRE("Temporaire");

    private final String libelle;

    StatutDisponibilite(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
