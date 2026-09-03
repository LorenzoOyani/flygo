package com.org.flygo.domain;

import com.org.flygo.dto.DocumentStatus;
import com.org.flygo.dto.DocumentType;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(
        name = "user_documents",
        uniqueConstraints = {
                // A user can only have ONE document row per document type.
                // Re-uploading the same type UPDATES this row, not a new insert.
                @UniqueConstraint(name = "uk_user_document_type", columnNames = {"user_id", "document_type"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "cloudinary_url", nullable = false, length = 1000)
    private String cloudinaryUrl;

    // Needed to delete/replace the file on Cloudinary later
    @Column(name = "cloudinary_public_id", nullable = false)
    private String cloudinaryPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    private String rejectionReason;
}
