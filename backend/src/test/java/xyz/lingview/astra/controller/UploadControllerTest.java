package xyz.lingview.astra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.dto.request.DownloadResourceDTO;
import xyz.lingview.astra.dto.request.UploadCompleteDTO;
import xyz.lingview.astra.dto.request.UploadInitDTO;
import xyz.lingview.astra.service.UploadService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UploadControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private UploadController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void uploadAttachment_shouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(uploadService.uploadAttachment(any(), any())).thenReturn(ApiResponse.success(Map.of("url", "/file/abc")));

        mockMvc.perform(multipart("/api/uploadattachment").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void initMultipartUpload_shouldReturnSuccess() throws Exception {
        UploadInitDTO dto = new UploadInitDTO();
        dto.setFileName("test.jpg");
        dto.setFileSize(1024L);
        dto.setFileType("image/jpeg");

        when(uploadService.initMultipartUpload(any(), any())).thenReturn(ApiResponse.success(Map.of("uploadId", "xxx")));

        mockMvc.perform(post("/api/uploadattachment/init")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void uploadAvatar_shouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "test".getBytes());
        when(uploadService.uploadAvatar(any(), any())).thenReturn(ApiResponse.success(Map.of("url", "/file/avatar")));

        mockMvc.perform(multipart("/api/uploadavatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminUploadAvatar_shouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "test".getBytes());
        when(uploadService.adminUploadAvatar(any(), any())).thenReturn(ApiResponse.success(Map.of("url", "/file/avatar")));

        mockMvc.perform(multipart("/api/admin/uploadavatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void downloadExternalResource_shouldReturnSuccess() throws Exception {
        DownloadResourceDTO dto = new DownloadResourceDTO();
        dto.setUrl("https://example.com/image.jpg");

        when(uploadService.downloadAndUploadExternalResource(any(), anyString()))
                .thenReturn(ApiResponse.success(Map.of("url", "/file/downloaded")));

        mockMvc.perform(post("/api/download-external-resource")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}