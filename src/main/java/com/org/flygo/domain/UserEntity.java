package com.org.flygo.domain;

import com.org.flygo.dto.OnBoardingState;
import com.org.flygo.dto.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Column(name = "full_name", unique = true, nullable = false)
    private String fullName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnBoardingState status;

    @NotBlank
    @Column(name = "password", length = 500)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRoles role = UserRoles.CUSTOMER;


}
