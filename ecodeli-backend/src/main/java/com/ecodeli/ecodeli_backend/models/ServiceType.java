package com.ecodeli.ecodeli_backend.models;

public enum ServiceType {
    TRANSPORT_LIVRAISON("🚚 TRANSPORT & LIVRAISON", "Transport de personnes, livraison de colis, déménagement, courses urgentes"),
    SERVICES_DOMICILE("🏠 SERVICES À DOMICILE", "Ménage, garde d'enfants/animaux, jardinage, assistance aux personnes âgées"),
    TRAVAUX_REPARATIONS("🔧 TRAVAUX & RÉPARATIONS", "Bricolage, plomberie, électricité, rénovation, assemblage de meubles"),
    COURSES_ACHATS("🛒 COURSES & ACHATS", "Courses alimentaires, achats divers, recherche de produits spécifiques"),
    SERVICES_PERSONNELS("👥 SERVICES PERSONNELS", "Assistance administrative, organisation d'événements, secrétariat, conciergerie"),
    EDUCATION_FORMATION("🎓 ÉDUCATION & FORMATION", "Cours particuliers, formation professionnelle, coaching, soutien scolaire");

    private final String libelle;
    private final String description;

    ServiceType(String libelle, String description) {
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
