package xyz.lingview.astra.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoleFormDTO {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private List<Integer> permissionIds;
}
