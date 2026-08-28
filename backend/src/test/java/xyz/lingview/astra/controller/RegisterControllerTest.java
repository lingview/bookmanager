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
import xyz.lingview.astra.dto.request.RegisterDTO;
import xyz.lingview.astra.dto.response.RegisterResponseDTO;
import xyz.lingview.astra.service.RegisterService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RegisterService registerService;

    @InjectMocks
    private RegisterController registerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(registerController).build();
    }

    @Test
    void register_shouldReturnSuccess() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setSuccess(true);
        response.setMessage("注册成功！");

        when(registerService.register(any(RegisterDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void register_shouldReturn400WhenServiceThrows() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("existing");
        dto.setPassword("123456");
        dto.setCaptcha("abcd");
        dto.setCaptchaKey("key123");

        when(registerService.register(any(RegisterDTO.class))).thenThrow(new RuntimeException("账号已存在！"));

        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("账号已存在！"));
    }
}