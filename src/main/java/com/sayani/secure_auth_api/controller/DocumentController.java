package com.sayani.secure_auth_api.controller;

import com.sayani.secure_auth_api.dto.DocumentResponse;
import com.sayani.secure_auth_api.entity.Document;
import com.sayani.secure_auth_api.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * Secure file upload endpoint.
     * Accessible by authenticated users with a valid JWT Bearer token.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String userEmail = authentication.getName();
        DocumentResponse response = documentService.uploadDocument(file, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Secure file download endpoint.
     * Streams the file from secure storage with original filename & MIME type headers.
     * Role-based access control: regular users can only download their own files; admins can download any.
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadDocument(@RequestParam Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        Document metadata = documentService.getDocumentMetadata(id, userEmail);
        Resource fileResource = documentService.downloadDocument(id, userEmail);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(metadata.getFileType());
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalFileName() + "\"")
                .body(fileResource);
    }

    /**
     * List all documents accessible to the authenticated user.
     * Regular users see only their uploaded files; Admin users see all files.
     */
    @GetMapping("/list-documens")
    public ResponseEntity<List<DocumentResponse>> listDocuments(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(documentService.listDocuments(userEmail));
    }

    /**
     * Get document metadata by ID.
     */
    @GetMapping("/get-document-by-id")
    public ResponseEntity<DocumentResponse> getDocumentMetadata(@RequestParam Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        Document doc = documentService.getDocumentMetadata(id, userEmail);
        DocumentResponse response = DocumentResponse.builder()
                .id(doc.getId())
                .fileName(doc.getOriginalFileName())
                .fileType(doc.getFileType())
                .fileSize(doc.getFileSize())
                .uploadedBy(doc.getUploadedBy().getEmail())
                .uploadedAt(doc.getUploadedAt())
                .downloadUrl("/api/v1/documents/download/" + doc.getId())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Secure document deletion.
     * Regular users can delete their own files; Admins can delete any file.
     */
    @DeleteMapping("/delete-document")
    public ResponseEntity<Map<String, Object>> deleteDocument(@RequestParam Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        documentService.deleteDocument(id, userEmail);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Document deleted successfully."
        ));
    }
}
