package com.sayani.secure_auth_api;

import com.sayani.secure_auth_api.dto.DocumentResponse;
import com.sayani.secure_auth_api.dto.RegisterRequest;
import com.sayani.secure_auth_api.entity.Role;
import com.sayani.secure_auth_api.entity.User;
import com.sayani.secure_auth_api.exception.CustomMessageException;
import com.sayani.secure_auth_api.repository.DocumentRepository;
import com.sayani.secure_auth_api.repository.UserRepository;
import com.sayani.secure_auth_api.service.AuthService;
import com.sayani.secure_auth_api.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocumentServiceTests {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
        userRepository.deleteAll();

        // Register regular user
        authService.register(RegisterRequest.builder()
                .name("Sayani Ghosh")
                .email("sayani@example.com")
                .password("Password@123")
                .build());

        // Register second user
        authService.register(RegisterRequest.builder()
                .name("Other User")
                .email("other@example.com")
                .password("Password@123")
                .build());

        // Register admin user
        User admin = User.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password("AdminPassword@123")
                .role(Role.ROLE_ADMIN)
                .build();
        userRepository.save(admin);
    }

    @Test
    void testUploadAndDownloadDocument_HappyPath() throws IOException {
        String fileContent = "This is a confidential security report.";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "security_report.pdf",
                "application/pdf",
                fileContent.getBytes(StandardCharsets.UTF_8)
        );

        // 1. Upload
        DocumentResponse uploadResponse = documentService.uploadDocument(file, "sayani@example.com");
        assertNotNull(uploadResponse);
        assertNotNull(uploadResponse.getId());
        assertEquals("security_report.pdf", uploadResponse.getFileName());
        assertEquals("sayani@example.com", uploadResponse.getUploadedBy());
        assertEquals(fileContent.length(), uploadResponse.getFileSize());

        // 2. Download by owner
        Resource resource = documentService.downloadDocument(uploadResponse.getId(), "sayani@example.com");
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals("security_report.pdf", resource.getFilename().substring(resource.getFilename().indexOf('_') + 1));

        // 3. List documents for owner
        List<DocumentResponse> documents = documentService.listDocuments("sayani@example.com");
        assertEquals(1, documents.size());
        assertEquals("security_report.pdf", documents.get(0).getFileName());
    }

    @Test
    void testRoleBasedAccessControl_RBAC() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "private_doc.txt",
                "text/plain",
                "Secret data".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse uploadResponse = documentService.uploadDocument(file, "sayani@example.com");
        Long docId = uploadResponse.getId();

        // Other regular user should NOT be able to download or access Sayani's document
        CustomMessageException ex = assertThrows(CustomMessageException.class, () -> {
            documentService.downloadDocument(docId, "other@example.com");
        });
        assertTrue(ex.getMessage().contains("Access denied"));

        // Admin user CAN access Sayani's document
        Resource adminResource = documentService.downloadDocument(docId, "admin@example.com");
        assertNotNull(adminResource);
        assertTrue(adminResource.exists());

        // Admin user can view all documents in listing
        List<DocumentResponse> allDocs = documentService.listDocuments("admin@example.com");
        assertEquals(1, allDocs.size());

        // Other user sees 0 documents in their listing
        List<DocumentResponse> otherDocs = documentService.listDocuments("other@example.com");
        assertEquals(0, otherDocs.size());
    }

    @Test
    void testDeleteDocument() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "temp.txt",
                "text/plain",
                "Temporary".getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse uploadResponse = documentService.uploadDocument(file, "sayani@example.com");
        Long docId = uploadResponse.getId();

        documentService.deleteDocument(docId, "sayani@example.com");

        // Verify document is deleted
        assertThrows(CustomMessageException.class, () -> {
            documentService.getDocumentMetadata(docId, "sayani@example.com");
        });
    }
}
