package xyz.lingview.astra.service;

import jakarta.servlet.http.HttpServletRequest;
import xyz.lingview.astra.dto.request.LoginDTO;
import xyz.lingview.astra.dto.response.LoginResponseDTO;
import xyz.lingview.astra.dto.response.LogoutResponseDTO;

public interface LoginService {
    LoginResponseDTO login(LoginDTO loginDTO, HttpServletRequest request);
    LogoutResponseDTO logout(String username, jakarta.servlet.http.HttpSession session);
}