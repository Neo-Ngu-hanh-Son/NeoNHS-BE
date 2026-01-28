package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.ChangePasswordRequest;
import fpt.project.NeoNHS.dto.request.LoginRequest;
import fpt.project.NeoNHS.dto.request.RegisterRequest;
import fpt.project.NeoNHS.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse googleLogin(String idToken) throws Exception;
    void changePassword(String email, ChangePasswordRequest request);
}
