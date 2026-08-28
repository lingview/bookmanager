package xyz.lingview.astra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.lingview.astra.dto.request.ForgotPasswordDTO;
import xyz.lingview.astra.dto.response.ForgotPasswordResponseDTO;
import xyz.lingview.astra.service.ForgotPasswordService;
import xyz.lingview.astra.common.ApiResponse;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ForgotPasswordService forgotPasswordService;

    @InjectMocks
    private ForgotPasswordController forgotPasswordController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(forgotPasswordController).build();
    }

    @Test
    void sendCaptcha_shouldReturnSuccess() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setUsername("admin");
        dto.setEmail("admin@test.com");

        when(forgotPasswordService.sendForgotPasswordCaptcha(any(), any(), any()))
                .thenReturn(ApiResponse.success(Map.of("message", "验证码已发送")));

        mockMvc.perform(post("/api/forgot-password/send-captcha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void sendCaptcha_shouldReturn500WhenServiceThrows() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setUsername("admin");
        dto.setEmail("admin@test.com");

        when(forgotPasswordService.sendForgotPasswordCaptcha(any(), any(), any()))
                .thenThrow(new RuntimeException("错误"));

        mockMvc.perform(post("/api/forgot-password/send-captcha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    void verifyCaptcha_shouldReturnSuccess() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setUsername("admin");
        dto.setCaptcha("123456");

        ForgotPasswordResponseDTO response = new ForgotPasswordResponseDTO();
        response.setSuccess(true);
        response.setMessage("验证成功");
        when(forgotPasswordService.verifyForgotPassword(any(), any(), any()))
                .thenReturn(ApiResponse.success(response));

        mockMvc.perform(post("/api/forgot-password/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void resetPassword_shouldReturnSuccess() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setUsername("admin");
        dto.setNewPassword("newpass123");

        ForgotPasswordResponseDTO response = new ForgotPasswordResponseDTO();
        response.setSuccess(true);
        response.setMessage("密码重置成功");
        when(forgotPasswordService.resetPassword(any(), any(), any()))
                .thenReturn(ApiResponse.success(response));

        mockMvc.perform(post("/api/forgot-password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void resetPassword_shouldReturn500WhenServiceThrows() throws Exception {
        ForgotPasswordDTO dto = new ForgotPasswordDTO();
        dto.setUsername("admin");
        dto.setNewPassword("newpass123");

        when(forgotPasswordService.resetPassword(any(), any(), any()))
                .thenThrow(new RuntimeException("重置失败"));

        mockMvc.perform(post("/api/forgot-password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}