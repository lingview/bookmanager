package xyz.lingview.astra.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.lingview.astra.service.FileAccessService;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileAccessController {

    @Autowired
    private FileAccessService fileAccessService;

    // 根据访问键获取文件（预览或下载）
    @GetMapping("/{accessKey}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String accessKey,
            @RequestParam(required = false, defaultValue = "false") Boolean download) {
        return fileAccessService.getFile(accessKey, download);
    }
}