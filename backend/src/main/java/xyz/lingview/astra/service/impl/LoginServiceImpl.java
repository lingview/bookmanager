package xyz.lingview.astra.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.domain.Login;
import xyz.lingview.astra.dto.request.LoginDTO;
import xyz.lingview.astra.dto.response.LoginResponseDTO;
import xyz.lingview.astra.dto.response.LogoutResponseDTO;
import xyz.lingview.astra.mapper.LoginMapper;
import xyz.lingview.astra.mapper.UserInformationMapper;
import xyz.lingview.astra.service.CacheService;
import xyz.lingview.astra.service.LoginService;
import xyz.lingview.astra.service.MailService;
import xyz.lingview.astra.service.NotificationService;
import xyz.lingview.astra.util.CaptchaUtil;
import xyz.lingview.astra.util.PasswordUtil;
import xyz.lingview.astra.util.SiteConfigUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    private static final String CAPTCHA_PREFIX = "captcha_";
    private static final String SESSION_CAPTCHA_KEY_ATTR = "captchaKey";

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserInformationMapper userInformationMapper;

    @Autowired
    private SiteConfigUtil siteConfigUtil;

    @Autowired
    private NotificationService notificationService;

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO, HttpServletRequest request) {
        HttpSession session = request.getSession();

        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String captcha = loginDTO.getCaptcha();
        String captchaKey = loginDTO.getCaptchaKey();

        // Validate captcha key match with session
        String sessionCaptchaKey = (String) session.getAttribute(SESSION_CAPTCHA_KEY_ATTR);
        log.info("登录请求 - Session ID: {}, 请求 captchaKey: {}, Session captchaKey: {}",
                session.getId(), captchaKey, sessionCaptchaKey);

        if (sessionCaptchaKey == null || !sessionCaptchaKey.equals(captchaKey)) {
            cleanUpCaptcha(session, CAPTCHA_PREFIX + captchaKey);
            throw new RuntimeException("验证码无效或已过期，请重新获取");
        }

        // Validate captcha from Redis
        String redisCaptchaKey = CAPTCHA_PREFIX + captchaKey;
        String redisCaptcha = cacheService.get(redisCaptchaKey, String.class);
        if (redisCaptcha == null) {
            cleanUpCaptcha(session, redisCaptchaKey);
            throw new RuntimeException("验证码已过期，请重新获取");
        }

        if (!CaptchaUtil.validateCaptcha(redisCaptcha, captcha)) {
            cleanUpCaptcha(session, redisCaptchaKey);
            throw new RuntimeException("验证码错误");
        }

        cleanUpCaptcha(session, redisCaptchaKey);

        // Validate user credentials
        Login login = new Login();
        login.setUsername(username);
        login.setPassword(password);

        var result = loginMapper.loginUser(login);
        if (result == null || !PasswordUtil.checkPassword(password, result.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // Create new session
        session.invalidate();
        session = request.getSession(true);
        session.setAttribute("username", username);
        session.setAttribute("isLoggedIn", true);
        session.setAttribute("loginTime", System.currentTimeMillis());
        session.setAttribute("userAgent", request.getHeader("User-Agent"));

        log.info("用户 {} 登录成功，Session ID: {}", username, session.getId());

        // Send notifications
        if (siteConfigUtil.isNotificationEnabled()) {
            String email = userInformationMapper.getEmailByUsername(username);
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            if (email != null && !email.trim().isEmpty()) {
                String siteName = siteConfigUtil.getSiteName();
                mailService.sendSimpleMail(email, siteName + " 登录成功",
                        "用户：" + username + " 于 " + formattedDate + " 登录成功");
            }
            notificationService.sendSystemNotification(username, "系统通知",
                    "用户：" + username + " 于 " + formattedDate + " 登录成功");
        }

        LoginResponseDTO data = new LoginResponseDTO();
        data.setSuccess(true);
        data.setMessage("登录成功");
        return data;
    }

    @Override
    public LogoutResponseDTO logout(String username, HttpSession session) {
        session.invalidate();
        log.info("用户 {} 登出成功", username != null ? username : "Unknown");

        LogoutResponseDTO data = new LogoutResponseDTO();
        data.setSuccess(true);
        data.setMessage("登出成功");
        return data;
    }

    private void cleanUpCaptcha(HttpSession session, String redisCaptchaKey) {
        try {
            cacheService.delete(redisCaptchaKey);
            session.removeAttribute(SESSION_CAPTCHA_KEY_ATTR);
        } catch (Exception e) {
            log.warn("清理验证码时发生异常", e);
        }
    }
}