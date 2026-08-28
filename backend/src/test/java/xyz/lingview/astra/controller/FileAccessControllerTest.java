package xyz.lingview.astra.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.lingview.astra.service.FileAccessService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FileAccessControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileAccessService fileAccessService;

    @InjectMocks
    private FileAccessController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getFile_shouldReturnResource() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("test content".getBytes());
        when(fileAccessService.getFile("abc123", false))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource));

        mockMvc.perform(get("/file/abc123"))
                .andExpect(status().isOk());
    }

    @Test
    void getFile_shouldHandleDownload() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("test content".getBytes());
        when(fileAccessService.getFile("abc123", true))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''test.jpg")
                        .body(resource));

        mockMvc.perform(get("/file/abc123").param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''test.jpg"));
    }
}