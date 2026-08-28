package xyz.lingview.astra.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface FileAccessService {
    ResponseEntity<Resource> getFile(String accessKey, Boolean download);
}