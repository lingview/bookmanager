package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuUpdateDTO {
    @NotBlank(message = "菜单ID不能为空")
    private String menus_id;
    private String menus_name;
    private String menus_url;
    private Integer sort_order;
    private Integer status;
}