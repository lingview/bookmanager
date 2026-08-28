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
import xyz.lingview.astra.dto.request.NotificationActionDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.domain.SystematicNotification;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.NotificationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SystematicNotificationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private NotificationService notificationService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private SystematicNotificationController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getSystematicNotification_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        PageResult<SystematicNotification> pageResult = new PageResult<>();
        pageResult.setData(List.of());
        pageResult.setTotal(0);
        pageResult.setPage(1);
        pageResult.setSize(10);
        pageResult.setTotal_pages(0);
        when(notificationService.getSystematicNotificationsByUsername(eq("admin"), anyInt(), anyInt()))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/getsystematicNotification")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getReadNotifications_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        PageResult<SystematicNotification> pageResult = new PageResult<>();
        pageResult.setData(List.of());
        when(notificationService.getReadNotificationsByUsername(eq("admin"), anyInt(), anyInt()))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/getReadNotifications")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getUnreadNotifications_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        PageResult<SystematicNotification> pageResult = new PageResult<>();
        pageResult.setData(List.of());
        when(notificationService.getUnreadNotificationsByUsername(eq("admin"), anyInt(), anyInt()))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/getUnreadNotifications")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void readSystematicNotification_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        when(notificationService.updateNotificationStatusByUsername(anyInt(), anyString())).thenReturn(true);

        NotificationActionDTO dto = new NotificationActionDTO();
        dto.setId(1);

        mockMvc.perform(post("/api/readSystematicNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteSystematicNotification_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        when(notificationService.deleteSystematicNotificationByUsername(anyInt(), anyString())).thenReturn(true);

        NotificationActionDTO dto = new NotificationActionDTO();
        dto.setId(1);

        mockMvc.perform(post("/api/deleteSystematicNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void readSystematicNotification_shouldReturn400WhenFail() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        when(notificationService.updateNotificationStatusByUsername(anyInt(), anyString())).thenReturn(false);

        NotificationActionDTO dto = new NotificationActionDTO();
        dto.setId(999);

        mockMvc.perform(post("/api/readSystematicNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}