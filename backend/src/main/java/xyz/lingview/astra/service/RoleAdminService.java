package xyz.lingview.astra.service;

import xyz.lingview.astra.domain.Permission;
import xyz.lingview.astra.domain.Role;
import xyz.lingview.astra.dto.request.RoleFormDTO;
import xyz.lingview.astra.dto.response.RoleAdminRowDTO;

import java.util.List;

public interface RoleAdminService {

    List<RoleAdminRowDTO> listRolesWithStats();

    List<Permission> listPermissions();

    Role getRole(Integer id);

    List<Integer> getPermissionIds(Integer roleId);

    void saveRole(RoleFormDTO form);

    void deleteRole(Integer roleId);
}
