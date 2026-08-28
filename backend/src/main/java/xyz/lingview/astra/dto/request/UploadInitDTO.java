package xyz.lingview.astra.dto.request;

import lombok.Data;

@Data
public class UploadInitDTO {
    private String fileName;
    private Long fileSize;
    private String fileType;
}