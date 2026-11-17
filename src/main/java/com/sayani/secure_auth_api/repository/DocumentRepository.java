package com.sayani.secure_auth_api.repository;

import com.sayani.secure_auth_api.entity.Document;
import com.sayani.secure_auth_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUploadedBy(User uploadedBy);

    Optional<Document> findByIdAndUploadedBy(Long id, User uploadedBy);

    Optional<Document> findByStoredFileName(String storedFileName);
}
