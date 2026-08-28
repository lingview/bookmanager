package xyz.lingview.astra.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.lingview.astra.dto.request.DashboardMenuDTO;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.DashboardMenuService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardMenuService dashboardMenuService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private DashboardController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getDashboardMenus_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        when(dashboardMenuService.getDashboardMenus("admin")).thenReturn(new DashboardMenuDTO());

        mockMvc.perform(get("/api/dashboard/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getDashboardMenus_shouldReturn500WhenError() throws Exception {
        when(currentUserService.getCurrentUsername()).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/dashboard/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}