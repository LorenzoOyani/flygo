package com.org.flygo.domain;

import com.org.flygo.dto.OnBoardingState;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users_Table")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String fullName;
    private String email;
    private OnBoardingState status;
    private String password;
}
