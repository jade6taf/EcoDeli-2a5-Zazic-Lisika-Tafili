package com.ecodeli.ecodeli_backend.models;

public enum ServiceType {
    TRANSPORT_PERSONNES("Transport de personnes"),
    ACHAT_ETRANGER("Achat à l'étranger"),
    GARDE_ANIMAUX("Garde d'animaux"),
    LIVRAISON_COURSES("Livraison de courses"),
    PETITS_TRAVAUX("Petits travaux");

    private final String libelle;

    ServiceType(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
