package xyz.lingview.astra.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.service.CaptchaService;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    // 获取验证码
    @GetMapping("/captcha")
    public ApiResponse<Map<String, String>> getCaptcha(HttpSession session) {
        try {
            Map<String, String> responseData = captchaService.generateCaptcha(session);
            return ApiResponse.success(responseData);
        } catch (Exception e) {
            log.error("验证码生成失败", e);
            return ApiResponse.error(500, "验证码生成失败");
        }
    }
}