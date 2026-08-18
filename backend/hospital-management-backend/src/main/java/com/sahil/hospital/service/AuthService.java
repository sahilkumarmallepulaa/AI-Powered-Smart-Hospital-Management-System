package com.sahil.hospital.service;

import com.sahil.hospital.dto.AuthResponse;
import com.sahil.hospital.dto.LoginRequest;
import com.sahil.hospital.dto.RegisterRequest;
import com.sahil.hospital.entity.Role;
import com.sahil.hospital.entity.User;
import com.sahil.hospital.exception.BadRequestException;
import com.sahil.hospital.exception.UnauthorizedException;
import com.sahil.hospital.repository.UserRepository;
import com.sahil.hospital.security.CustomUserDetailsService;
import com.sahil.hospital.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting registration for username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        Role assignedRole = request.getRole();
        if (assignedRole == Role.ADMIN || assignedRole == Role.DOCTOR) {
            throw new BadRequestException("Self-registration as ADMIN or DOCTOR is not allowed");
        }
        if (assignedRole == null) {
            assignedRole = Role.PATIENT;
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .role(assignedRole)
            .enabled(true)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}, role: {}", savedUser.getId(), savedUser.getRole());

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .role(savedUser.getRole().name())
            .expiresInMs(jwtService.getJwtExpirationMs())
            .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsernameOrEmail());

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsernameOrEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException | DisabledException e) {
            log.warn("Authentication failed for user {}: {}", request.getUsernameOrEmail(), e.getMessage());
            throw new UnauthorizedException("Invalid username/email or password");
        }

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
            .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password")));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole().name())
            .expiresInMs(jwtService.getJwtExpirationMs())
            .build();
    }
}
