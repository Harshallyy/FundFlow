package com.fundflow.service;

import com.fundflow.dto.user.UserResponse;
import com.fundflow.entity.User;

import java.util.List;

public interface UserService {

    /** Internal lookup used by other services (e.g. to attach an organizer/donor to an entity). */
    User getUserEntity(Long userId);

    UserResponse getProfile(Long userId);

    /** Admin-only: list users, optionally filtered by role (DONOR / ORGANIZER / ADMIN). */
    List<UserResponse> listUsers(String roleFilter);
}
