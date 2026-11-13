package xyz.lingview.astra.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.lingview.astra.domain.Permission;
import xyz.lingview.astra.domain.Role;
import xyz.lingview.astra.dto.response.RoleAdminRowDTO;

import java.util.List;

@Mapper
public interface RoleAdminMapper {

    List<RoleAdminRowDTO> selectRolesWithStats();

    Role selectRoleById(@Param("id") Integer id);

    Role selectRoleByCode(@Param("code") String code);

    int insertRole(Role role);

    int updateRole(Role role);

    int deleteRole(@Param("id") Integer id);

    List<Permission> selectAllPermissions();

    List<Integer> selectPermissionIdsByRole(@Param("roleId") Integer roleId);

    int deleteRolePermissions(@Param("roleId") Integer roleId);

    int insertRolePermission(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);

    int countUsersByRole(@Param("roleId") Integer roleId);

    int countRegisterDefaultRole(@Param("roleId") Integer roleId);
}
