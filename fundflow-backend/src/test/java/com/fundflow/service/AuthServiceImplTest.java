package com.fundflow.service;

import com.fundflow.dto.auth.RegisterRequest;
import com.fundflow.entity.Role;
import com.fundflow.entity.User;
import com.fundflow.exception.DuplicateResourceException;
import com.fundflow.repository.RoleRepository;
import com.fundflow.repository.UserRepository;
import com.fundflow.security.JwtUtil;
import com.fundflow.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_newDonor_hashesPasswordAndAssignsDonorRole() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Jane Donor");
        request.setEmail("jane@test.com");
        request.setPassword("SuperSecret1");
        request.setRole("DONOR");

        Role donorRole = Role.builder().id(1L).name("ROLE_DONOR").build();

        when(userRepository.existsByEmail("jane@test.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_DONOR")).thenReturn(Optional.of(donorRole));
        when(passwordEncoder.encode("SuperSecret1")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(1L, "jane@test.com", "ROLE_DONOR")).thenReturn("fake-jwt");

        var response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("fake-jwt");
        assertThat(response.getRole()).isEqualTo("ROLE_DONOR");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed-password");
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Jane Donor");
        request.setEmail("jane@test.com");
        request.setPassword("SuperSecret1");
        request.setRole("DONOR");

        when(userRepository.existsByEmail("jane@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
