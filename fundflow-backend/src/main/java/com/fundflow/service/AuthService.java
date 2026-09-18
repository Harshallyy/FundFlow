package com.fundflow.service;

import com.fundflow.dto.auth.AuthResponse;
import com.fundflow.dto.auth.LoginRequest;
import com.fundflow.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
