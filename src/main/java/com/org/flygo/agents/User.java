package com.org.flygo.agents;

import com.org.flygo.dto.OnBoardingState;
import com.org.flygo.dto.UserRoles;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private UUID id;

    private String fullName;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OnBoardingState status = OnBoardingState.DOCUMENTS_REQUIRED;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRoles role = UserRoles.CUSTOMER;
}
