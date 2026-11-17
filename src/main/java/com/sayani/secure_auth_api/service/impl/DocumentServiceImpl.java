package com.sayani.secure_auth_api.service.impl;

import com.sayani.secure_auth_api.dto.DocumentResponse;
import com.sayani.secure_auth_api.entity.Document;
import com.sayani.secure_auth_api.entity.Role;
import com.sayani.secure_auth_api.entity.User;
import com.sayani.secure_auth_api.exception.CustomMessageException;
import com.sayani.secure_auth_api.repository.DocumentRepository;
import com.sayani.secure_auth_api.repository.UserRepository;
import com.sayani.secure_auth_api.service.DocumentService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final Path fileStorageLocation;

    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            @Value("${app.document.upload-dir:uploads/documents}") String uploadDir
    ) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Secure document storage directory initialized at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new CustomMessageException("Could not create storage directory for documents: " + ex.getMessage());
        }
    }

    @Override
    public DocumentResponse uploadDocument(MultipartFile file, String userEmail) {
        if (file == null || file.isEmpty()) {
            throw new CustomMessageException("Please select a file to upload.");
        }

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFileName.contains("..")) {
            throw new CustomMessageException("Filename contains invalid path sequence: " + originalFileName);
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomMessageException("Authenticated user not found: " + userEmail));

        // Generate UUID-based unique filename for storage to avoid collision & path traversal
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        Path targetLocation = this.fileStorageLocation.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new CustomMessageException("Failed to store file: " + originalFileName + ". " + ex.getMessage());
        }

        Document document = Document.builder()
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .fileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .fileSize(file.getSize())
                .filePath(targetLocation.toString())
                .uploadedBy(user)
                .build();

        Document savedDoc = documentRepository.save(document);

        return mapToResponse(savedDoc, "Document uploaded successfully.");
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId, String userEmail) {
        Document document = getAuthorizedDocument(documentId, userEmail);

        try {
            Path filePath = Paths.get(document.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new CustomMessageException("Document file not found on disk: " + document.getOriginalFileName());
            }
        } catch (MalformedURLException ex) {
            throw new CustomMessageException("Malformed file path for document: " + document.getOriginalFileName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Document getDocumentMetadata(Long documentId, String userEmail) {
        return getAuthorizedDocument(documentId, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> listDocuments(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomMessageException("Authenticated user not found: " + userEmail));

        // Role-Based Access Control: Admins can view all documents, regular users view only their own
        List<Document> documents;
        if (user.getRole() == Role.ROLE_ADMIN) {
            documents = documentRepository.findAll();
        } else {
            documents = documentRepository.findByUploadedBy(user);
        }

        return documents.stream()
                .map(doc -> mapToResponse(doc, null))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteDocument(Long documentId, String userEmail) {
        Document document = getAuthorizedDocument(documentId, userEmail);

        try {
            Path filePath = Paths.get(document.getFilePath()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.warn("Could not delete physical file: {}", document.getFilePath());
        }

        documentRepository.delete(document);
    }

    /**
     * Enforces Role-Based Access Control (RBAC):
     * Admins have full access. Regular users can only access documents they uploaded.
     */
    private Document getAuthorizedDocument(Long documentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomMessageException("Authenticated user not found: " + userEmail));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new CustomMessageException("Document not found with ID: " + documentId));

        if (user.getRole() != Role.ROLE_ADMIN && !document.getUploadedBy().getId().equals(user.getId())) {
            throw new CustomMessageException("Access denied: You do not have permission to access this document.");
        }

        return document;
    }

    private DocumentResponse mapToResponse(Document doc, String message) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .fileName(doc.getOriginalFileName())
                .fileType(doc.getFileType())
                .fileSize(doc.getFileSize())
                .uploadedBy(doc.getUploadedBy() != null ? doc.getUploadedBy().getEmail() : "Unknown")
                .uploadedAt(doc.getUploadedAt())
                .downloadUrl("/api/v1/documents/download/" + doc.getId())
                .message(message)
                .build();
    }
}
