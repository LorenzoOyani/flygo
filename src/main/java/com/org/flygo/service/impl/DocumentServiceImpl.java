package com.org.flygo.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.org.flygo.domain.UserDocument;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.DocumentStatus;
import com.org.flygo.dto.DocumentType;
import com.org.flygo.dto.OnBoardingState;
import com.org.flygo.exception.DocumentNotFoundException;
import com.org.flygo.exception.UserNotFoundException;
import com.org.flygo.persistence.UserDocumentRepository;
import com.org.flygo.persistence.UserRepository;
import com.org.flygo.service.DocumentService;
import com.org.flygo.util.FileValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "application/pdf"
    );
    private final UserDocumentRepository userDocumentRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public UserDocument uploadDocument(UUID userId, DocumentType documentType, MultipartFile file) throws IOException {
        FileValidator.validateImageFile(file);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserDocument document = userDocumentRepository
                .findByUserAndDocumentType(user, documentType)
                .orElse(null);

        // If replacing an existing document, remove the old file from Cloudinary first
        if (document != null) {
            cloudinary.uploader().destroy(document.getCloudinaryPublicId(), ObjectUtils.emptyMap());
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "auto",
                        "folder", "flygo/documents/" + userId
                )
        );

        String secureUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        if (document == null) {
            document = UserDocument.builder()
                    .user(user)
                    .documentType(documentType)
                    .cloudinaryUrl(secureUrl)
                    .cloudinaryPublicId(publicId)
                    .status(DocumentStatus.PENDING_REVIEW)
                    .build();
        } else {
            document.setCloudinaryUrl(secureUrl);
            document.setCloudinaryPublicId(publicId);
            document.setStatus(DocumentStatus.PENDING_REVIEW);
            document.setRejectionReason(null);
        }

        userDocumentRepository.save(document);

        logger.info("Document {} uploaded for user {}", documentType, userId);

        advanceOnboardingStatusIfNeeded(user);

        return document;

    }

    private void advanceOnboardingStatusIfNeeded(UserEntity user) {
        if (user.getStatus() == OnBoardingState.DOCUMENTS_REQUIRED) {
            user.setStatus(OnBoardingState.DOCUMENTS_SUBMITTED);  // grants dashboard access
            userRepository.save(user);
            // The Document itself still sits as PENDING_REVIEW — reviewed separately, doesn't block access
        }
    }

    @Override

    public List<UserDocument> getDocumentsForUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userDocumentRepository.findAllByUser(user);
    }

    @Override
    @Transactional
    public void deleteDocument(UUID userId, DocumentType documentType) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserDocument document = userDocumentRepository
                .findByUserAndDocumentType(user, documentType)
                .orElseThrow(() -> new DocumentNotFoundException(
                        "No " + documentType + " document found for this user"
                ));

        cloudinary.uploader().destroy(document.getCloudinaryPublicId(), ObjectUtils.emptyMap());

        userDocumentRepository.delete(document);

        logger.info("Document {} deleted for user {}", documentType, userId);
    }
}
