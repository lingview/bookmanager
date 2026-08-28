package xyz.lingview.astra.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.lingview.astra.annotation.RequiresPermission;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.dto.request.DownloadResourceDTO;
import xyz.lingview.astra.dto.request.UploadCompleteDTO;
import xyz.lingview.astra.dto.request.UploadInitDTO;
import xyz.lingview.astra.service.UploadService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    // 上传附件
    @PostMapping("/uploadattachment")
    @RequiresPermission("attachment:add")
    public ApiResponse<Map<String, String>> uploadAttachment(HttpServletRequest request,
                                                             @RequestParam("file") MultipartFile file) {
        return uploadService.uploadAttachment(request, file);
    }

    // 初始化分片上传
    @PostMapping("/uploadattachment/init")
    @RequiresPermission("attachment:add")
    public ApiResponse<Map<String, String>> initMultipartUpload(HttpServletRequest request,
                                                                @RequestBody UploadInitDTO dto) {
        Map<String, String> payload = new HashMap<>();
        if (dto.getFileName() != null) payload.put("fileName", dto.getFileName());
        if (dto.getFileSize() != null) payload.put("fileSize", String.valueOf(dto.getFileSize()));
        if (dto.getFileType() != null) payload.put("fileType", dto.getFileType());
        return uploadService.initMultipartUpload(request, payload);
    }

    // 上传分片
    @PostMapping("/uploadattachment/part")
    @RequiresPermission("attachment:add")
    public ApiResponse<Map<String, String>> uploadChunk(
            HttpServletRequest request,
            @RequestHeader("Upload-Id") String uploadId,
            @RequestHeader("Chunk-Index") int chunkIndex,
            @RequestBody byte[] chunkData) {
        return uploadService.uploadChunk(request, uploadId, chunkIndex, chunkData);
    }

    // 完成分片上传
    @PostMapping("/uploadattachment/complete")
    @RequiresPermission("attachment:add")
    public ApiResponse<Map<String, String>> completeUpload(
            HttpServletRequest request,
            @RequestBody UploadCompleteDTO dto) {
        Map<String, String> payload = new HashMap<>();
        if (dto.getFileName() != null) payload.put("fileName", dto.getFileName());
        if (dto.getFileId() != null) payload.put("fileId", dto.getFileId());
        if (dto.getTotalChunks() != null) payload.put("totalChunks", String.valueOf(dto.getTotalChunks()));
        if (dto.getFileExtension() != null) payload.put("fileExtension", dto.getFileExtension());
        return uploadService.completeUpload(request, payload);
    }

    // 上传头像
    @PostMapping("/uploadavatar")
    public ApiResponse<Map<String, String>> uploadAvatar(HttpServletRequest request,
                                                         @RequestParam("file") MultipartFile file) {
        return uploadService.uploadAvatar(request, file);
    }

    // 管理员上传用户头像
    @PostMapping("/admin/uploadavatar")
    @RequiresPermission("system:user:management")
    public ApiResponse<Map<String, String>> adminUploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        return uploadService.adminUploadAvatar(request, file);
    }

    // 代理下载外部资源
    @PostMapping("/download-external-resource")
    @RequiresPermission("attachment:add")
    public ApiResponse<Map<String, String>> downloadExternalResource(
            HttpServletRequest request,
            @RequestBody @Valid DownloadResourceDTO dto) {
        return uploadService.downloadAndUploadExternalResource(request, dto.getUrl());
    }


}