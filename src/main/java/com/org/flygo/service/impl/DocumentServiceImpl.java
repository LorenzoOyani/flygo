package com.org.flygo.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.org.flygo.domain.UserDocument;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.DocumentStatus;
import com.org.flygo.dto.DocumentType;
import com.org.flygo.dto.OnBoardingState;
import com.org.flygo.dto.VerificationResult;
import com.org.flygo.exception.DocumentNotFoundException;
import com.org.flygo.exception.UserNotFoundException;
import com.org.flygo.persistence.UserDocumentRepository;
import com.org.flygo.persistence.UserRepository;
import com.org.flygo.service.DocumentService;
import com.org.flygo.service.DocumentVerificationService;
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

    private final UserDocumentRepository userDocumentRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final DocumentVerificationService documentVerificationService;

    @Override
    @Transactional
    public UserDocument uploadDocument(UUID userId, DocumentType documentType, MultipartFile file) throws IOException {
        FileValidator.validateDocumentFile(documentType, file);

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

        VerificationResult verificationResult = documentVerificationService.verify(document);
        applyVerification(document, verificationResult);

        syncOnboardingStatus(user);

        return document;

    }

    private void syncOnboardingStatus(UserEntity user) {
        boolean hasApprovedDocument = userDocumentRepository.findAllByUser(user).stream()
                .anyMatch(d -> d.getStatus() == DocumentStatus.APPROVED);

        user.setStatus(hasApprovedDocument
                ? OnBoardingState.DOCUMENTS_SUBMITTED
                : OnBoardingState.DOCUMENTS_REQUIRED);
        userRepository.save(user);
    }

    private void applyVerification(UserDocument document, VerificationResult verificationResult) {

        switch (verificationResult.verificationOutcome()){

            case APPROVED ->document.setStatus(DocumentStatus.APPROVED);
            case REJECTED -> {
                document.setStatus(DocumentStatus.REJECTED);
                document.setRejectionReason(verificationResult.reason());
            }


            case NEEDS_MANUAL_REVIEW -> document.setStatus(DocumentStatus.PENDING_REVIEW);
        }

        userDocumentRepository.save(document);

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
