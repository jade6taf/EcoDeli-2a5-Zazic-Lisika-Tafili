package com.ecodeli.ecodeli_backend.services;

import com.ecodeli.ecodeli_backend.models.Justificatif;
import com.ecodeli.ecodeli_backend.models.Utilisateur;
import com.ecodeli.ecodeli_backend.repositories.JustificatifRepository;
import com.ecodeli.ecodeli_backend.repositories.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final JustificatifRepository justificatifRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final Path uploadDirectory = Paths.get("uploads/documents");

    public DocumentService(JustificatifRepository justificatifRepository, UtilisateurRepository utilisateurRepository) {
        this.justificatifRepository = justificatifRepository;
        this.utilisateurRepository = utilisateurRepository;
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload", e);
        }
    }

    @Transactional
    public Justificatif saveDocument(MultipartFile file, String type, Integer userId) throws IOException {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        String fileName = UUID.randomUUID().toString() + ".pdf";
        Path filePath = uploadDirectory.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        Justificatif justificatif = new Justificatif();
        justificatif.setTypeJustificatif(type);
        justificatif.setCheminFichier(fileName);
        justificatif.setDateDebut(LocalDateTime.now());
        justificatif.setUtilisateur(utilisateur);
        justificatif.setValidationParAd(false);

        return justificatifRepository.save(justificatif);
    }

    public List<Justificatif> getPendingDocuments() {
        return justificatifRepository.findByValidationParAdFalseAndCommentaireIsNull();
    }

    public List<Justificatif> getApprovedDocuments() {
        return justificatifRepository.findByValidationParAdTrue();
    }

    public List<Justificatif> getRejectedDocuments() {
        return justificatifRepository.findByValidationParAdFalseAndCommentaireIsNotNull();
    }

    public List<Justificatif> getUserDocuments(Integer userId) {
        return justificatifRepository.findByUtilisateur_IdUtilisateur(userId);
    }

    @Transactional
    public Justificatif approveDocument(Integer id) {
        Justificatif justificatif = justificatifRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Document non trouvé"));

        justificatif.setValidationParAd(true);
        justificatif.setDateFin(LocalDateTime.now());
        justificatif.setCommentaire(null);

        return justificatifRepository.save(justificatif);
    }

    @Transactional
    public Justificatif rejectDocument(Integer id, String commentaire) {
        Justificatif justificatif = justificatifRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Document non trouvé"));

        justificatif.setValidationParAd(false);
        justificatif.setDateFin(LocalDateTime.now());
        justificatif.setCommentaire(commentaire);

        return justificatifRepository.save(justificatif);
    }

    @Transactional
    public void deleteDocument(Integer id) throws IOException {
        Justificatif justificatif = justificatifRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Document non trouvé"));

        Path filePath = uploadDirectory.resolve(justificatif.getCheminFichier());
        Files.deleteIfExists(filePath);

        justificatifRepository.delete(justificatif);
    }

    public Resource loadDocumentAsResource(Integer id) throws IOException {
        Justificatif justificatif = justificatifRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Document non trouvé"));
        Path filePath = uploadDirectory.resolve(justificatif.getCheminFichier());

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Fichier non trouvé: " + justificatif.getCheminFichier());
        }

        byte[] content = Files.readAllBytes(filePath);
        return new ByteArrayResource(content);
    }
}
