package com.org.flygo.agents;

import com.org.flygo.dto.OnBoardingState;
import com.org.flygo.dto.UserRoles;
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

    private OnBoardingState status;

    private UserRoles role;
}
