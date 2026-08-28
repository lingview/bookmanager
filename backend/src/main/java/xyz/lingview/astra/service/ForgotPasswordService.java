package xyz.lingview.astra.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import xyz.lingview.astra.dto.request.ForgotPasswordDTO;
import xyz.lingview.astra.dto.response.ForgotPasswordResponseDTO;
import xyz.lingview.astra.common.ApiResponse;

import java.util.Map;

/**
 * @Author: lingview
 * @Date: 2026/01/13 21:19:46
 * @Description: 忘记密码服务接口
 * @Version: 1.0
 */
public interface ForgotPasswordService {

    /**
     * 发送忘记密码验证码到邮箱
     */
    ApiResponse<Map<String, Object>> sendForgotPasswordCaptcha(
            ForgotPasswordDTO request,
            HttpSession session,
            HttpServletRequest httpRequest);

    /**
     * 验证验证码
     */
    ApiResponse<ForgotPasswordResponseDTO> verifyForgotPassword(
            ForgotPasswordDTO request,
            HttpSession session,
            HttpServletRequest httpRequest);

    /**
     * 重置密码
     */
    ApiResponse<ForgotPasswordResponseDTO> resetPassword(
            ForgotPasswordDTO request,
            HttpSession session,
            HttpServletRequest httpRequest);
}