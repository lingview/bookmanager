package xyz.lingview.astra.dto.response;

import lombok.Data;

@Data
public class RoleAdminRowDTO {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Byte status;
    private Integer permission_count;
    private Integer user_count;
}
