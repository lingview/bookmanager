package xyz.lingview.astra.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.lingview.astra.domain.UserInformation;
import xyz.lingview.astra.dto.request.RegisterDTO;
import xyz.lingview.astra.dto.response.RegisterResponseDTO;
import xyz.lingview.astra.mapper.RegisterMapper;
import xyz.lingview.astra.mapper.SiteConfigMapper;
import xyz.lingview.astra.mapper.UserInformationMapper;
import xyz.lingview.astra.mapper.UserRoleMapper;
import xyz.lingview.astra.service.impl.RegisterServiceImpl;
import xyz.lingview.astra.util.SiteConfigUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceImplTest {

    @Mock
    private RegisterMapper registerMapper;

    @Mock
    private CacheService cacheService;

    @Mock
    private SiteConfigMapper siteConfigMapper;

    @Mock
    private SiteConfigUtil siteConfigUtil;

    @Mock
    private MailService mailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserInformationMapper userInformationMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private RegisterServiceImpl registerService;

    @Test
    void register_shouldThrowWhenRegistrationDisabled() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        when(siteConfigMapper.getEnableRegister()).thenReturn(0);

        assertThrows(RuntimeException.class, () -> registerService.register(dto));
    }

    @Test
    void register_shouldThrowWhenCaptchaExpired() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        when(siteConfigMapper.getEnableRegister()).thenReturn(1);
        when(cacheService.get("captcha_key123", String.class)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> registerService.register(dto));
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("existing");
        dto.setPassword("123456");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        when(siteConfigMapper.getEnableRegister()).thenReturn(1);
        when(cacheService.get("captcha_key123", String.class)).thenReturn("abcd");
        when(registerMapper.selectUser("existing")).thenReturn(1);

        assertThrows(RuntimeException.class, () -> registerService.register(dto));
    }

    @Test
    void register_shouldReturnSuccess() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        when(siteConfigMapper.getEnableRegister()).thenReturn(1);
        when(cacheService.get("captcha_key123", String.class)).thenReturn("abcd");
        when(registerMapper.selectUser("newuser")).thenReturn(0);
        when(registerMapper.insertUser(any())).thenReturn(1);

        UserInformation user = new UserInformation();
        user.setId(1);
        when(userInformationMapper.getUserByUsername("newuser")).thenReturn(user);
        when(siteConfigMapper.getRegisterUserPermission()).thenReturn(2);
        when(siteConfigUtil.isNotificationEnabled()).thenReturn(false);

        RegisterResponseDTO result = registerService.register(dto);
        assertTrue(result.isSuccess());
        assertEquals("注册成功！", result.getMessage());
    }
}