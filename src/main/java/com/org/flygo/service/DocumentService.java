package com.org.flygo.service;

import com.org.flygo.domain.UserDocument;
import com.org.flygo.dto.DocumentType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


public interface DocumentService {

    UserDocument uploadDocument(UUID userId, DocumentType documentType, MultipartFile file) throws IOException;

    List<UserDocument> getDocumentsForUser(UUID userId);

    void deleteDocument(UUID userId, DocumentType documentType) throws IOException;
}
