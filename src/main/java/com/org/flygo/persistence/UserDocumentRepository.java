package com.org.flygo.persistence;

import com.org.flygo.domain.UserDocument;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDocumentRepository extends JpaRepository<UserDocument, String> {
    Optional<UserDocument> findByUserAndDocumentType(UserEntity user, DocumentType documentType);

    List<UserDocument> findAllByUser(UserEntity user);
}
