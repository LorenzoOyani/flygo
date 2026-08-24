package com.org.flygo.security;

import com.org.flygo.domain.UserEntity;
import com.org.flygo.persistence.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    @NonNull
    public UserDetails loadUserByUsername( @NonNull String email) throws UsernameNotFoundException {

        UserEntity user =
                userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("user with this " + email + " not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
