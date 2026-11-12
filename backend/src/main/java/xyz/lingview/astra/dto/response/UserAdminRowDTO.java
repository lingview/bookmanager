package xyz.lingview.astra.dto.response;

import lombok.Data;
import xyz.lingview.astra.domain.Role;

import java.util.List;

@Data
public class UserAdminRowDTO {
    private Integer id;
    private String uuid;
    private String username;
    private String email;
    private String phone;
    private String create_time;
    private List<Role> roles;
    private List<Integer> role_ids;
}
