package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuSortItemDTO {
    @NotBlank(message = "菜单ID不能为空")
    private String menus_id;
    @NotNull(message = "排序不能为空")
    private Integer sort_order;
}