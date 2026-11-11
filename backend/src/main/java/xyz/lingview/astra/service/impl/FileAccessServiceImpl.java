package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.domain.UploadAttachment;
import xyz.lingview.astra.mapper.UploadMapper;
import xyz.lingview.astra.service.FileAccessService;
import xyz.lingview.astra.service.ImageCompressionService;
import xyz.lingview.astra.service.SiteConfigService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class FileAccessServiceImpl implements FileAccessService {

    @Autowired
    private UploadMapper uploadMapper;

    @Autowired
    private ImageCompressionService imageCompressionService;

    @Autowired
    private SiteConfigService siteConfigService;

    @Value("${file.data-root:.}")
    private String dataRoot;

    @Override
    public ResponseEntity<Resource> getFile(String accessKey, Boolean download) {
        try {
            log.debug("尝试访问文件，访问键: {}, 下载模式: {}", accessKey, download);

            UploadAttachment attachment = uploadMapper.selectByAccessKey(accessKey);
            if (attachment == null) {
                log.warn("未找到访问键对应的文件: {}", accessKey);
                return ResponseEntity.notFound().build();
            }

            Path basePath = Path.of(dataRoot).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(attachment.getAttachment_path()).normalize();

            if (!Files.exists(filePath)) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null || "application/octet-stream".equalsIgnoreCase(contentType)) {
                contentType = guessContentType(filePath.getFileName().toString(), contentType);
            }

            boolean isImage = contentType.startsWith("image/");
            Resource resource;

            if (isImage && !download) {
                Integer enableCompression = siteConfigService.getEnableImageCompression();
                boolean shouldCompress = enableCompression != null && enableCompression == 1;

                if (shouldCompress) {
                    Path compressedPath = getCompressedImagePath(filePath);
                    if (Files.exists(compressedPath)) {
                        log.debug("使用压缩图片: {}", compressedPath);
                        resource = new FileSystemResource(compressedPath.toFile());
                    } else {
                        log.debug("压缩图片不存在，使用原图并触发异步压缩: {}", filePath);
                        resource = new FileSystemResource(filePath.toFile());
                        imageCompressionService.compressImageAsync(filePath.toString());
                    }
                } else {
                    log.debug("图片压缩已禁用，使用原图: {}", filePath);
                    resource = new FileSystemResource(filePath.toFile());
                }
            } else {
                resource = new FileSystemResource(filePath.toFile());
            }

            String filename = filePath.getFileName().toString();
            String disposition;
            if (download) {
                disposition = "attachment; filename*=UTF-8''" + encodeFilename(filename);
            } else {
                disposition = "inline; filename*=UTF-8''" + encodeFilename(filename);
            }

            log.debug("成功找到文件: {}, 模式: {}", filename, download ? "下载" : "预览");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .body(resource);

        } catch (Exception e) {
            log.error("获取文件时发生错误，访问键: {}", accessKey, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String guessContentType(String fileName, String fallback) {
        int dot = fileName.lastIndexOf('.');
        String extension = dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            case "svg" -> "image/svg+xml";
            case "mp3" -> "audio/mpeg";
            case "m4a" -> "audio/mp4";
            case "aac" -> "audio/aac";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "flac" -> "audio/flac";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "json" -> "application/json";
            case "zip" -> "application/zip";
            default -> fallback != null ? fallback : "application/octet-stream";
        };
    }

    private String encodeFilename(String filename) {        try {
            return URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error("编码文件名失败: {}", filename, e);
            return filename;
        }
    }

    private Path getCompressedImagePath(Path originalPath) {
        Path parentDir = originalPath.getParent();
        String fileName = originalPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        String compressedFileName = baseName + "_compressed.jpg";
        return parentDir.resolve("compressor").resolve(compressedFileName);
    }
}