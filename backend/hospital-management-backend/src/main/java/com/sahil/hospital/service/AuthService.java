package com.sahil.hospital.service;

import com.sahil.hospital.dto.AuthResponse;
import com.sahil.hospital.dto.LoginRequest;
import com.sahil.hospital.dto.UserResponse;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.UnauthorizedException;
import com.sahil.hospital.repository.UserRepository;
import com.sahil.hospital.security.CustomUserDetailsService;
import com.sahil.hospital.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();
        log.info("Attempting authentication for user: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.findByUsername(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password")));

        if (!user.isEnabled()) {
            log.warn("Login attempt rejected for disabled account: {}", email);
            throw new UnauthorizedException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", email);
            throw new UnauthorizedException("Invalid email or password");
        }

        String lookupKey = (user.getUsername() != null && !user.getUsername().isBlank()) ? user.getUsername() : user.getEmail();
        UserDetails userDetails = userDetailsService.loadUserByUsername(lookupKey);
        String token = jwtService.generateToken(userDetails);

        UserResponse userResponse = userService.mapToUserResponse(user);

        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtService.getJwtExpirationMs())
            .user(userResponse)
            .build();
    }
}

