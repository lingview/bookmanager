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
import xyz.lingview.astra.dto.request.*;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.MenusService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MenusControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MenusService menusService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private MenusController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void frontendGetMenus_shouldReturnSuccess() throws Exception {
        when(menusService.getAllMenus()).thenReturn(List.of());

        mockMvc.perform(get("/api/frontendgetmenus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getMenus_shouldReturnSuccess() throws Exception {
        when(menusService.getAllMenus()).thenReturn(List.of());

        mockMvc.perform(get("/api/getmenus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void addMenus_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");

        MenuCreateDTO dto = new MenuCreateDTO();
        dto.setMenus_name("首页");
        dto.setMenus_url("/");

        mockMvc.perform(post("/api/addmenus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void addMenus_shouldReturn401WhenNotLoggedIn() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn(null);

        MenuCreateDTO dto = new MenuCreateDTO();
        dto.setMenus_name("首页");
        dto.setMenus_url("/");

        mockMvc.perform(post("/api/addmenus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void editMenus_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");

        MenuUpdateDTO dto = new MenuUpdateDTO();
        dto.setMenus_id("menu_xxx");
        dto.setMenus_name("新首页");

        mockMvc.perform(post("/api/editmenus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteMenus_shouldReturnSuccess() throws Exception {
        MenuDeleteDTO dto = new MenuDeleteDTO();
        dto.setMenus_id("menu_xxx");

        mockMvc.perform(post("/api/deletemenus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void updateSortOrder_shouldReturnSuccess() throws Exception {
        MenuSortItemDTO item = new MenuSortItemDTO();
        item.setMenus_id("menu_xxx");
        item.setSort_order(1);

        mockMvc.perform(post("/api/updatesortorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(item))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}