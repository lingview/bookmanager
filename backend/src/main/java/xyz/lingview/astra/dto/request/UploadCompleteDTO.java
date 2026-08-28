package xyz.lingview.astra.dto.request;

import lombok.Data;

@Data
public class UploadCompleteDTO {
    private String fileName;
    private String fileId;
    private Integer totalChunks;
    private String fileExtension;
}