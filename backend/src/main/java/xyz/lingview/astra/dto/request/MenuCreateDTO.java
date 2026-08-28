package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuCreateDTO {
    @NotBlank(message = "菜单名称不能为空")
    private String menus_name;
    @NotBlank(message = "菜单URL不能为空")
    private String menus_url;
    private Integer sort_order;
}