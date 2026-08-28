package xyz.lingview.astra.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.dto.request.LoginDTO;
import xyz.lingview.astra.dto.response.LoginResponseDTO;
import xyz.lingview.astra.dto.response.LogoutResponseDTO;
import xyz.lingview.astra.service.LoginService;

@Slf4j
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginService loginService;

    // 用户登录
    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(
            @RequestBody LoginDTO loginDTO,
            HttpServletRequest request) {
        try {
            String username = loginDTO.getUsername();
            String password = loginDTO.getPassword();
            String captcha = loginDTO.getCaptcha();
            String captchaKey = loginDTO.getCaptchaKey();

            if (username == null || username.trim().isEmpty()) {
                return ApiResponse.error(400, "用户名不能为空");
            }
            if (password == null || password.isEmpty()) {
                return ApiResponse.error(400, "密码不能为空");
            }
            if (captcha == null || captcha.isEmpty()) {
                return ApiResponse.error(400, "验证码不能为空");
            }
            if (captchaKey == null || captchaKey.isEmpty()) {
                return ApiResponse.error(400, "验证码无效");
            }

            LoginResponseDTO data = loginService.login(loginDTO, request);
            return ApiResponse.success(data);
        } catch (RuntimeException e) {
            log.warn("登录失败: {}", e.getMessage());
            return ApiResponse.error(401, e.getMessage());
        } catch (Exception e) {
            log.error("登录过程发生未知错误", e);
            return ApiResponse.error(500, "登录失败，请稍后再试");
        }
    }

    // 用户登出
    @PostMapping("/logout")
    public ApiResponse<LogoutResponseDTO> logout(HttpSession session) {
        String username = (String) session.getAttribute("username");
        LogoutResponseDTO data = loginService.logout(username, session);
        return ApiResponse.success(data);
    }
}