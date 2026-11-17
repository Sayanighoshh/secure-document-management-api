package com.sayani.secure_auth_api.service;

import com.sayani.secure_auth_api.dto.DocumentResponse;
import com.sayani.secure_auth_api.entity.Document;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse uploadDocument(MultipartFile file, String userEmail);

    Resource downloadDocument(Long documentId, String userEmail);

    Document getDocumentMetadata(Long documentId, String userEmail);

    List<DocumentResponse> listDocuments(String userEmail);

    void deleteDocument(Long documentId, String userEmail);
}
