package com.ecodeli.analytics.enums;

public enum ServiceType {
    TRANSPORT_LIVRAISON("🚚 TRANSPORT & LIVRAISON", "Transport de personnes, livraison de colis, déménagement, courses urgentes"),
    SERVICES_DOMICILE("🏠 SERVICES À DOMICILE", "Ménage, garde d'enfants/animaux, jardinage, assistance aux personnes âgées"),
    TRAVAUX_REPARATIONS("🔧 TRAVAUX & RÉPARATIONS", "Bricolage, plomberie, électricité, rénovation, assemblage de meubles"),
    COURSES_ACHATS("🛒 COURSES & ACHATS", "Courses alimentaires, achats divers, recherche de produits spécifiques"),
    SERVICES_PERSONNELS("👥 SERVICES PERSONNELS", "Assistance administrative, organisation d'événements, secrétariat, conciergerie"),
    EDUCATION_FORMATION("🎓 ÉDUCATION & FORMATION", "Cours particuliers, formation professionnelle, coaching, soutien scolaire");

    private final String label;
    private final String description;

    ServiceType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return label.substring(0, label.indexOf(' '));
    }

    public String getDisplayName() {
        return label.substring(label.indexOf(' ') + 1);
    }
}
