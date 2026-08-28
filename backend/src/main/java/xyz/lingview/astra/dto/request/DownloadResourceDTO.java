package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DownloadResourceDTO {
    @NotBlank(message = "资源URL不能为空")
    private String url;
}