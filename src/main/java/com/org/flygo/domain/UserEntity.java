package com.org.flygo.domain;

import com.org.flygo.dto.OnBoardingState;
import com.org.flygo.dto.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "password", length = 500)
    private String password;

//    @Enumerated(EnumType.STRING)
//    @Builder.Default
//    private UserRoles role = UserRoles.CUSTOMER;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<UserRoles> roles = new HashSet<>(Set.of(UserRoles.CUSTOMER));

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;


}
