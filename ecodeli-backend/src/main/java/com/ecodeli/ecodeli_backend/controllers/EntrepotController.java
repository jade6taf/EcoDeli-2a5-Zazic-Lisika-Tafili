package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.services.EntrepotUtilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entrepots")
@CrossOrigin(origins = "http://localhost:5173")
public class EntrepotController {

    private final EntrepotUtilService entrepotUtilService;

    public EntrepotController(EntrepotUtilService entrepotUtilService) {
        this.entrepotUtilService = entrepotUtilService;
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<String>> getEntrepotsDisponibles() {
        List<String> entrepots = entrepotUtilService.getEntrepotsDisponibles();
        return ResponseEntity.ok(entrepots);
    }

    @GetMapping("/plus-proche")
    public ResponseEntity<String> getEntrepotLePlusProche(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        String entrepot = entrepotUtilService.getEntrepotLePlusProche(latitude, longitude);
        return ResponseEntity.ok(entrepot);
    }
}
