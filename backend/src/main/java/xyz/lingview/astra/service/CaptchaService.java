package xyz.lingview.astra.service;

import java.util.Map;

public interface CaptchaService {
    Map<String, String> generateCaptcha(jakarta.servlet.http.HttpSession session);
}