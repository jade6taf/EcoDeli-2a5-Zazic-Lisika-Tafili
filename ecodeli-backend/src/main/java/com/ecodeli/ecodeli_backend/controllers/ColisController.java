package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Colis;
import com.ecodeli.ecodeli_backend.services.ColisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/colis")
@CrossOrigin(origins = "http://localhost:5173")
public class ColisController {

    private final ColisService colisService;

    public ColisController(ColisService colisService) {
        this.colisService = colisService;
    }

    @GetMapping
    public ResponseEntity<List<Colis>> getAllColis() {
        List<Colis> colis = colisService.getAllColis();
        return ResponseEntity.ok(colis);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Colis> getColisById(@PathVariable Integer id) {
        Optional<Colis> colis = colisService.getColisById(id);
        return colis.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createColis(@RequestBody Colis colis) {
        try {
            Colis newColis = colisService.createColis(colis);
            return new ResponseEntity<>(newColis, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateColis(@PathVariable Integer id, @RequestBody Colis colis) {
        try {
            Colis updatedColis = colisService.updateColis(id, colis);
            return ResponseEntity.ok(updatedColis);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteColis(@PathVariable Integer id) {
        try {
            colisService.deleteColis(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}