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
import xyz.lingview.astra.dto.request.LoginDTO;
import xyz.lingview.astra.dto.response.LoginResponseDTO;
import xyz.lingview.astra.dto.response.LogoutResponseDTO;
import xyz.lingview.astra.service.LoginService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LoginService loginService;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    void login_shouldReturnSuccess() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        LoginResponseDTO response = new LoginResponseDTO();
        response.setSuccess(true);
        response.setMessage("登录成功");

        when(loginService.login(any(LoginDTO.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void login_shouldReturn400WhenUsernameEmpty() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("");
        dto.setPassword("123456");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名不能为空"));
    }

    @Test
    void login_shouldReturn401WhenServiceThrows() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrong");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        when(loginService.login(any(LoginDTO.class), any())).thenThrow(new RuntimeException("用户名或密码错误"));

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void logout_shouldReturnSuccess() throws Exception {
        LogoutResponseDTO response = new LogoutResponseDTO();
        response.setSuccess(true);
        response.setMessage("登出成功");

        when(loginService.logout(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }
}