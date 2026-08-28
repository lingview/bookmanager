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
import xyz.lingview.astra.service.AttachmentManagementService;
import xyz.lingview.astra.service.CurrentUserService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AttachmentManagementControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AttachmentManagementService attachmentManagementService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AttachmentManagementController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void adminGetAttachments_shouldReturnSuccess() throws Exception {
        when(attachmentManagementService.getPage(anyInt(), anyInt())).thenReturn(Map.of("data", "test"));

        mockMvc.perform(get("/api/attachments/admin/page")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminGetDeletedAttachmentsOnly_shouldReturnSuccess() throws Exception {
        when(attachmentManagementService.getDeletedPageOnly(anyInt(), anyInt())).thenReturn(Map.of("data", "test"));

        mockMvc.perform(get("/api/attachments/admin/page-deleted-only")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminGetAttachmentsWithDeleted_shouldReturnSuccess() throws Exception {
        when(attachmentManagementService.getPageWithRecentDeleted(anyInt(), anyInt())).thenReturn(Map.of("data", "test"));

        mockMvc.perform(get("/api/attachments/admin/page-with-deleted")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminGetAttachmentsByUser_shouldReturnSuccess() throws Exception {
        when(attachmentManagementService.getPageByUserUuid(anyString(), anyInt(), anyInt())).thenReturn(Map.of("data", "test"));

        mockMvc.perform(get("/api/attachments/admin/page-by-user")
                .param("userUuid", "uuid-xxx")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void userGetAttachments_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUserUuid()).thenReturn("user-uuid");
        when(attachmentManagementService.getPageByUuid(anyString(), anyInt(), anyInt())).thenReturn(Map.of("data", "test"));

        mockMvc.perform(get("/api/attachments/page")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void userGetDeletedAttachmentsOnly_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUserUuid()).thenReturn("user-uuid");
        when(attachmentManagementService.getDeletedPageByUuidOnly(anyString(), anyInt(), anyInt())).thenReturn(Map.of("data", "test"));

        mockMvc.perform(get("/api/attachments/page-deleted-only")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void userGetAttachmentsWithDeleted_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUserUuid()).thenReturn("user-uuid");
        when(attachmentManagementService.getPageByUuidWithRecentDeleted(anyString(), anyInt(), anyInt())).thenReturn(Map.of("data", "test"));

        mockMvc.perform(get("/api/attachments/page-with-deleted")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminDeleteAttachment_shouldReturnSuccess() throws Exception {
        when(attachmentManagementService.deleteAttachment(anyString())).thenReturn(true);

        mockMvc.perform(delete("/api/attachments/admin/attach-xxx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminDeleteAttachment_shouldReturn400WhenFail() throws Exception {
        when(attachmentManagementService.deleteAttachment(anyString())).thenReturn(false);

        mockMvc.perform(delete("/api/attachments/admin/attach-xxx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void adminRestoreAttachment_shouldReturnSuccess() throws Exception {
        when(attachmentManagementService.restoreAttachment(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/attachments/admin/attach-xxx/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminRestoreAttachment_shouldReturn400WhenFail() throws Exception {
        when(attachmentManagementService.restoreAttachment(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/attachments/admin/attach-xxx/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // User delete/restore with ownership check
    @Test
    void userDeleteAttachment_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        when(attachmentManagementService.deleteAttachmentByUser(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(delete("/api/attachments/attach-xxx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void userDeleteAttachment_shouldReturn403WhenUnauthorized() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        when(attachmentManagementService.deleteAttachmentByUser(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(delete("/api/attachments/attach-xxx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void userRestoreAttachment_shouldReturnSuccess() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        when(attachmentManagementService.restoreAttachmentByUser(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/attachments/attach-xxx/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void userRestoreAttachment_shouldReturn403WhenUnauthorized() throws Exception {
        when(currentUserService.getCurrentUsername()).thenReturn("admin");
        when(attachmentManagementService.restoreAttachmentByUser(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/attachments/attach-xxx/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}