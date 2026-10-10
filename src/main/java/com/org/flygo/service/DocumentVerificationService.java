package com.org.flygo.service;

import com.org.flygo.domain.UserDocument;
import com.org.flygo.dto.VerificationResult;

public interface DocumentVerificationService {

    VerificationResult verify(UserDocument userDocument);
}
