package xyz.lingview.astra.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import xyz.lingview.astra.domain.Login;
import xyz.lingview.astra.dto.request.LoginDTO;
import xyz.lingview.astra.dto.response.LoginResponseDTO;
import xyz.lingview.astra.dto.response.LogoutResponseDTO;
import xyz.lingview.astra.mapper.LoginMapper;
import xyz.lingview.astra.mapper.UserInformationMapper;
import xyz.lingview.astra.service.impl.LoginServiceImpl;
import xyz.lingview.astra.util.CaptchaUtil;
import xyz.lingview.astra.util.PasswordUtil;
import xyz.lingview.astra.util.SiteConfigUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private LoginMapper loginMapper;

    @Mock
    private CacheService cacheService;

    @Mock
    private MailService mailService;

    @Mock
    private UserInformationMapper userInformationMapper;

    @Mock
    private SiteConfigUtil siteConfigUtil;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LoginServiceImpl loginService;

    @Test
    void login_shouldThrowWhenCaptchaExpired() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("captchaKey", "key123");
        request.setSession(session);

        when(cacheService.get("captcha_key123", String.class)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> loginService.login(dto, request));
    }

    @Test
    void login_shouldThrowWhenCaptchaMismatch() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        dto.setCaptcha("wrong");
        dto.setCaptchaKey("key123");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("captchaKey", "key123");
        request.setSession(session);

        when(cacheService.get("captcha_key123", String.class)).thenReturn("abcd");

        assertThrows(RuntimeException.class, () -> loginService.login(dto, request));
    }

    @Test
    void login_shouldThrowWhenCredentialsInvalid() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrong");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("captchaKey", "key123");
        request.setSession(session);

        when(cacheService.get("captcha_key123", String.class)).thenReturn("abcd");

        Login loginResult = new Login();
        loginResult.setUsername("admin");
        loginResult.setPassword("$2a$10$hash");
        when(loginMapper.loginUser(any(Login.class))).thenReturn(loginResult);

        assertThrows(RuntimeException.class, () -> loginService.login(dto, request));
    }

    @Test
    void logout_shouldReturnSuccess() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "admin");

        LogoutResponseDTO result = loginService.logout("admin", session);
        assertTrue(result.isSuccess());
        assertEquals("登出成功", result.getMessage());
    }

    @Test
    void logout_shouldHandleNullUsername() {
        MockHttpSession session = new MockHttpSession();
        LogoutResponseDTO result = loginService.logout(null, session);
        assertTrue(result.isSuccess());
    }
}