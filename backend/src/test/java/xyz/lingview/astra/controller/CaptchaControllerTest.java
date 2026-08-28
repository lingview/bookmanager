package xyz.lingview.astra.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.lingview.astra.service.CaptchaService;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CaptchaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private CaptchaController captchaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(captchaController).build();
    }

    @Test
    void getCaptcha_shouldReturnSuccess() throws Exception {
        when(captchaService.generateCaptcha(any(HttpSession.class)))
                .thenReturn(Map.of("image", "data:image/png;base64,abc123", "key", "captcha_key_123"));

        mockMvc.perform(get("/api/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.image").isString())
                .andExpect(jsonPath("$.data.key").isString());
    }

    @Test
    void getCaptcha_shouldReturn500WhenServiceThrows() throws Exception {
        when(captchaService.generateCaptcha(any(HttpSession.class))).thenThrow(new RuntimeException("生成失败"));

        mockMvc.perform(get("/api/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}