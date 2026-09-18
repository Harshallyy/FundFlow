package com.fundflow.service.impl;

import com.fundflow.dto.auth.AuthResponse;
import com.fundflow.dto.auth.LoginRequest;
import com.fundflow.dto.auth.RegisterRequest;
import com.fundflow.entity.Role;
import com.fundflow.entity.User;
import com.fundflow.exception.DuplicateResourceException;
import com.fundflow.exception.ResourceNotFoundException;
import com.fundflow.repository.RoleRepository;
import com.fundflow.repository.UserRepository;
import com.fundflow.security.CustomUserDetails;
import com.fundflow.security.JwtUtil;
import com.fundflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        // Self-registration is only ever DONOR or ORGANIZER - admins are seeded
        // directly in the database, never created through this endpoint.
        String roleName = "ROLE_" + request.getRole().toUpperCase();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), role.getName());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(role.getName())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = principal.getUser();
        String role = user.getRole().getName();

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), role);
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(role)
                .build();
    }
}
