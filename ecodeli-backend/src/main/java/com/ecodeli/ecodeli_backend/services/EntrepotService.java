package com.ecodeli.ecodeli_backend.services;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class EntrepotService {

    private static final Map<String, String> ENTREPOTS = Map.of(
        "Paris", "110, rue de Flandre, 19ème arrondissement",
        "Marseille", "Entrepôt EcoDeli Marseille",
        "Lyon", "Entrepôt EcoDeli Lyon",
        "Lille", "Entrepôt EcoDeli Lille",
        "Montpellier", "Entrepôt EcoDeli Montpellier",
        "Rennes", "Entrepôt EcoDeli Rennes"
    );

    public List<EntrepotDTO> getEntrepotsDisponibles() {
        return ENTREPOTS.entrySet().stream()
            .map(entry -> new EntrepotDTO(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(EntrepotDTO::getVille))
            .toList();
    }

    public String getAdresseEntrepot(String ville) {
        return ENTREPOTS.get(ville);
    }

    public boolean entrepotExiste(String ville) {
        return ENTREPOTS.containsKey(ville);
    }

    public static class EntrepotDTO {
        private String ville;
        private String adresse;

        public EntrepotDTO(String ville, String adresse) {
            this.ville = ville;
            this.adresse = adresse;
        }

        public String getVille() {
            return ville;
        }

        public void setVille(String ville) {
            this.ville = ville;
        }

        public String getAdresse() {
            return adresse;
        }

        public void setAdresse(String adresse) {
            this.adresse = adresse;
        }
    }
}
