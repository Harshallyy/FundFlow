package com.fundflow.service.impl;

import com.fundflow.dto.user.UserResponse;
import com.fundflow.entity.User;
import com.fundflow.exception.ResourceNotFoundException;
import com.fundflow.repository.UserRepository;
import com.fundflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @Override
    public UserResponse getProfile(Long userId) {
        User user = getUserEntity(userId);
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public List<UserResponse> listUsers(String roleFilter) {
        List<User> users = StringUtils.hasText(roleFilter)
                ? userRepository.findByRole_Name("ROLE_" + roleFilter.toUpperCase())
                : userRepository.findAll();

        return users.stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole().getName())
                        .enabled(u.isEnabled())
                        .createdAt(u.getCreatedAt())
                        .build())
                .toList();
    }
}
