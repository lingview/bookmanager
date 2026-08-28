package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.service.CacheService;
import xyz.lingview.astra.service.CaptchaService;
import xyz.lingview.astra.util.CaptchaUtil;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private CacheService cacheService;

    @Override
    public Map<String, String> generateCaptcha(HttpSession session) {
        String captcha = CaptchaUtil.generateCaptcha(4);
        String captchaKey = CaptchaUtil.generateCaptchaKey();

        // 删除旧的验证码（如果存在）
        String oldCaptchaKey = (String) session.getAttribute("captchaKey");
        if (oldCaptchaKey != null) {
            cacheService.delete("captcha_" + oldCaptchaKey);
        }

        // 保存新验证码到缓存（有效期5分钟）
        cacheService.set("captcha_" + captchaKey, captcha.toLowerCase(), 5, TimeUnit.MINUTES);

        // 存储新的验证码key到session
        session.setAttribute("captchaKey", captchaKey);

        // 生成验证码图片
        String captchaImage = CaptchaUtil.generateCaptchaImage(captcha);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("image", "data:image/png;base64," + captchaImage);
        responseData.put("key", captchaKey);

        return responseData;
    }
}