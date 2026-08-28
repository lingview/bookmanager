package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationActionDTO {
    @NotNull(message = "通知ID不能为空")
    private Integer id;
}