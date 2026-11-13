package xyz.lingview.astra.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.lingview.astra.domain.Permission;
import xyz.lingview.astra.domain.Role;
import xyz.lingview.astra.dto.request.RoleFormDTO;
import xyz.lingview.astra.dto.response.RoleAdminRowDTO;
import xyz.lingview.astra.mapper.RoleAdminMapper;
import xyz.lingview.astra.service.RoleAdminService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleAdminServiceImpl implements RoleAdminService {

    private static final String ADMIN_CODE = "ADMIN";

    private static final Set<String> ASSIGNABLE_CODES = Set.of(
            "book:upload", "book:edit", "book:delete", "book:batch", "category:manage",
            "user:view", "user:manage", "role:view", "role:manage",
            "user:menus", "role:menus", "content:menus");

    @Autowired
    private RoleAdminMapper roleAdminMapper;

    @Override
    public List<RoleAdminRowDTO> listRolesWithStats() {
        return roleAdminMapper.selectRolesWithStats();
    }

    @Override
    public List<Permission> listPermissions() {
        return roleAdminMapper.selectAllPermissions();
    }

    @Override
    public List<Permission> listAssignablePermissions() {
        return roleAdminMapper.selectAllPermissions().stream()
                .filter(permission -> ASSIGNABLE_CODES.contains(permission.getCode()))
                .collect(Collectors.toList());
    }

    @Override
    public Role getRole(Integer id) {
        return id == null ? null : roleAdminMapper.selectRoleById(id);
    }

    @Override
    public List<Integer> getPermissionIds(Integer roleId) {
        return roleId == null ? List.of() : roleAdminMapper.selectPermissionIdsByRole(roleId);
    }

    @Override
    @Transactional
    public void saveRole(RoleFormDTO form) {
        String name = form.getName() == null ? "" : form.getName().trim();
        if (name.isEmpty()) {
            throw new RuntimeException("角色名称不能为空");
        }
        if (name.length() > 100) {
            throw new RuntimeException("角色名称不能超过 100 个字符");
        }
        String description = form.getDescription() == null ? null : form.getDescription().trim();
        int status = form.getStatus() != null && form.getStatus() == 0 ? 0 : 1;

        if (form.getId() == null) {
            String code = form.getCode() == null ? "" : form.getCode().trim();
            if (code.isEmpty()) {
                throw new RuntimeException("角色编码不能为空");
            }
            if (!code.matches("^[A-Za-z0-9_:\\-]{2,50}$")) {
                throw new RuntimeException("角色编码只能包含字母、数字、下划线、冒号和短横线");
            }
            if (roleAdminMapper.selectRoleByCode(code) != null) {
                throw new RuntimeException("角色编码已存在");
            }
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setDescription(description);
            role.setStatus((byte) status);
            roleAdminMapper.insertRole(role);
            replacePermissions(role.getId(), form.getPermissionIds());
            return;
        }

        Role existing = roleAdminMapper.selectRoleById(form.getId());
        if (existing == null) {
            throw new RuntimeException("角色不存在");
        }
        boolean admin = ADMIN_CODE.equalsIgnoreCase(existing.getCode());
        existing.setName(name);
        existing.setDescription(description);
        existing.setStatus(admin ? (byte) 1 : (byte) status);
        roleAdminMapper.updateRole(existing);
        if (!admin) {
            replacePermissions(existing.getId(), form.getPermissionIds());
        }
    }

    @Override
    @Transactional
    public void deleteRole(Integer roleId) {
        Role role = roleAdminMapper.selectRoleById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        if (ADMIN_CODE.equalsIgnoreCase(role.getCode())) {
            throw new RuntimeException("管理员角色不能删除");
        }
        int users = roleAdminMapper.countUsersByRole(roleId);
        if (users > 0) {
            throw new RuntimeException("该角色已被 " + users + " 个用户使用，不能删除");
        }
        if (roleAdminMapper.countRegisterDefaultRole(roleId) > 0) {
            throw new RuntimeException("该角色是注册默认角色，请先修改注册设置");
        }
        roleAdminMapper.deleteRolePermissions(roleId);
        roleAdminMapper.deleteRole(roleId);
    }

    private void replacePermissions(Integer roleId, List<Integer> permissionIds) {
        roleAdminMapper.deleteRolePermissions(roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        Set<Integer> valid = listAssignablePermissions().stream()
                .map(Permission::getId).collect(Collectors.toSet());
        for (Integer permissionId : new LinkedHashSet<>(permissionIds)) {
            if (permissionId != null && valid.contains(permissionId)) {
                roleAdminMapper.insertRolePermission(roleId, permissionId);
            }
        }
    }
}
