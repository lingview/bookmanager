package xyz.lingview.astra.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import xyz.lingview.astra.service.impl.CaptchaServiceImpl;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceImplTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private CaptchaServiceImpl captchaService;

    @Test
    void generateCaptcha_shouldReturnImageAndKey() {
        MockHttpSession session = new MockHttpSession();

        Map<String, String> result = captchaService.generateCaptcha(session);

        assertNotNull(result.get("image"));
        assertNotNull(result.get("key"));
        assertTrue(result.get("image").startsWith("data:image/png;base64,"));
        assertNotNull(session.getAttribute("captchaKey"));
    }

    @Test
    void generateCaptcha_shouldStoreInCache() {
        MockHttpSession session = new MockHttpSession();

        captchaService.generateCaptcha(session);

        String captchaKey = (String) session.getAttribute("captchaKey");
        assertNotNull(captchaKey);
        verify(cacheService).set(eq("captcha_" + captchaKey), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void generateCaptcha_shouldCleanOldCaptcha() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("captchaKey", "old_key");

        captchaService.generateCaptcha(session);

        verify(cacheService).delete("captcha_" + "old_key");
    }
}