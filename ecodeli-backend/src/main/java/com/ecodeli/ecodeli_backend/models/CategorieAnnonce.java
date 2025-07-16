package com.ecodeli.ecodeli_backend.models;

public enum CategorieAnnonce {
    LIVRAISON_PONCTUELLE("Livraison ponctuelle", "Unique or occasional delivery needs"),
    SERVICE_CHARIOT("Service \"Lâcher de chariot\"", "Merchants offering home delivery services"),
    TRANSPORT_MARCHANDISES("Transport de marchandises", "Regular or bulk volume transportation needs");

    private final String libelle;
    private final String description;

    CategorieAnnonce(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }
}