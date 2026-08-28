package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuDeleteDTO {
    @NotBlank(message = "菜单ID不能为空")
    private String menus_id;
}