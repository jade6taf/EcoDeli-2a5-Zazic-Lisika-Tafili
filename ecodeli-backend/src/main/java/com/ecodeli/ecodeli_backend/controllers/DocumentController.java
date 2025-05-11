package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.models.Justificatif;
import com.ecodeli.ecodeli_backend.services.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/documents/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("userId") Integer userId) {
        try {
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Seuls les fichiers PDF sont acceptés"));
            }

            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "La taille du fichier ne doit pas dépasser 5MB"));
            }

            Justificatif justificatif = documentService.saveDocument(file, type, userId);
            return ResponseEntity.ok(justificatif);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erreur lors de l'upload du document: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/documents/pending")
    public ResponseEntity<List<Justificatif>> getPendingDocuments() {
        return ResponseEntity.ok(documentService.getPendingDocuments());
    }

    @GetMapping("/admin/documents/approved")
    public ResponseEntity<List<Justificatif>> getApprovedDocuments() {
        return ResponseEntity.ok(documentService.getApprovedDocuments());
    }

    @GetMapping("/admin/documents/rejected")
    public ResponseEntity<List<Justificatif>> getRejectedDocuments() {
        return ResponseEntity.ok(documentService.getRejectedDocuments());
    }

    @PutMapping("/admin/documents/{id}/approve")
    public ResponseEntity<?> approveDocument(@PathVariable Integer id) {
        try {
            Justificatif justificatif = documentService.approveDocument(id);
            return ResponseEntity.ok(justificatif);
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/admin/documents/{id}/reject")
    public ResponseEntity<?> rejectDocument(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        try {
            String commentaire = payload.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Le commentaire est obligatoire"));
            }

            Justificatif justificatif = documentService.rejectDocument(id, commentaire);
            return ResponseEntity.ok(justificatif);
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/documents/{userId}")
    public ResponseEntity<List<Justificatif>> getUserDocuments(@PathVariable Integer userId) {
        return ResponseEntity.ok(documentService.getUserDocuments(userId));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Integer id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin/documents/view/{id}")
    public ResponseEntity<?> viewDocument(@PathVariable Integer id) {
        try {
            Resource resource = documentService.loadDocumentAsResource(id);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("X-Frame-Options", "SAMEORIGIN")
                .header("Content-Disposition", "inline; filename=\"document.pdf\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }
}
