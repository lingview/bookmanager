package xyz.lingview.astra.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.lingview.astra.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author: lingview
 * @Date: 2025/11/12 16:08:35
 * @Description: 文件上传服务
 * @Version: 1.0
 */
public interface UploadService {
    ApiResponse<Map<String, String>> uploadAttachment(HttpServletRequest request, MultipartFile file);
    ApiResponse<Map<String, String>> initMultipartUpload(HttpServletRequest request, Map<String, String> payload);
    ApiResponse<Map<String, String>> uploadChunk(HttpServletRequest request, String uploadId, int chunkIndex, byte[] chunkData);
    ApiResponse<Map<String, String>> completeUpload(HttpServletRequest request, Map<String, String> payload);
    ApiResponse<Map<String, String>> uploadAvatar(HttpServletRequest request, MultipartFile file);
    ApiResponse<Map<String, String>> adminUploadAvatar(HttpServletRequest request, MultipartFile file);
    ApiResponse<Map<String, String>> downloadAndUploadExternalResource(HttpServletRequest request, String url);
}