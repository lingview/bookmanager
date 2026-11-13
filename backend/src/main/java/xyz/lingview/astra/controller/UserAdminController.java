package xyz.lingview.astra.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.lingview.astra.domain.Permission;
import xyz.lingview.astra.domain.Role;
import xyz.lingview.astra.dto.request.RoleFormDTO;
import xyz.lingview.astra.security.CsrfTokenService;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.RoleAdminService;
import xyz.lingview.astra.service.SiteConfigService;
import xyz.lingview.astra.service.UserAdminService;
import xyz.lingview.astra.service.UserPermissionCheckService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private RoleAdminService roleAdminService;

    @Autowired
    private UserPermissionCheckService permissionCheckService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private CsrfTokenService csrfTokenService;

    @Autowired
    private SiteConfigService siteConfigService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(required = false) Integer editRole,
                       HttpSession session, Model model,
                       HttpServletResponse response) {
        if (!canViewUsers()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "error/403";
        }
        boolean admin = isAdmin();
        model.addAttribute("pageResult", userAdminService.listUsers(page, 20));
        model.addAttribute("roles", userAdminService.listRoles());
        model.addAttribute("enableRegister", siteConfigService.getEnableRegister());
        model.addAttribute("registerRoleId", siteConfigService.getRegisterUserPermission());
        model.addAttribute("currentUsername", currentUserService.getCurrentUsername());
        model.addAttribute("canManage", admin);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("canManageUsers", canManageUsers());
        model.addAttribute("canManageRoles", canManageRoles());
        model.addAttribute("showRolesMenu", canViewRoleSection());
        model.addAttribute("rolesWithStats", roleAdminService.listRolesWithStats());
        model.addAttribute("permissions", roleAdminService.listPermissions());
        model.addAttribute("permissionGroups", groupPermissions(roleAdminService.listPermissions()));
        model.addAttribute("moduleLabels", moduleLabels());
        if (editRole != null) {
            Role role = editRole > 0 ? roleAdminService.getRole(editRole) : null;
            model.addAttribute("editRoleId", editRole);
            model.addAttribute("editRole", role);
            model.addAttribute("editPermissionIds",
                    role == null ? List.of() : roleAdminService.getPermissionIds(role.getId()));
        }
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "admin/users";
    }

    @PostMapping("/{id}/roles")
    public String updateRoles(@PathVariable Integer id,
                              @RequestParam(required = false) List<Integer> roleIds,
                              RedirectAttributes redirectAttributes,
                              HttpServletResponse response) {
        if (!canManageUsers()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "error/403";
        }
        try {
            userAdminService.updateRoles(id, roleIds);
            redirectAttributes.addFlashAttribute("flashMessage", "角色已更新");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Integer id,
                             RedirectAttributes redirectAttributes,
                             HttpServletResponse response) {
        if (!canManageUsers()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "error/403";
        }
        try {
            userAdminService.deleteUser(id, currentUserService.getCurrentUsername());
            redirectAttributes.addFlashAttribute("flashMessage", "用户已删除");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/register-settings")
    public String updateRegisterSettings(@RequestParam(defaultValue = "0") Integer enableRegister,
                                         @RequestParam(required = false) String registerRoleId,
                                         RedirectAttributes redirectAttributes,
                                         HttpServletResponse response) {
        if (!canManageUsers()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "error/403";
        }
        Integer roleId = null;
        if (registerRoleId != null && !registerRoleId.trim().isEmpty()) {
            try {
                roleId = Integer.valueOf(registerRoleId.trim());
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("flashError", "默认角色无效");
                return "redirect:/admin/users";
            }
        }
        if (siteConfigService.updateRegisterSettings(enableRegister, roleId)) {
            redirectAttributes.addFlashAttribute("flashMessage", "注册设置已保存");
        } else {
            redirectAttributes.addFlashAttribute("flashError", "注册设置保存失败");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/roles/save")
    public String saveRole(@ModelAttribute RoleFormDTO form,
                           RedirectAttributes redirectAttributes,
                           HttpServletResponse response) {
        if (!canManageRoles()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "error/403";
        }
        try {
            roleAdminService.saveRole(form);
            redirectAttributes.addFlashAttribute("flashMessage",
                    form.getId() == null ? "角色已创建" : "角色已更新");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/roles/{id}/delete")
    public String deleteRole(@PathVariable Integer id,
                             RedirectAttributes redirectAttributes,
                             HttpServletResponse response) {
        if (!canManageRoles()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "error/403";
        }
        try {
            roleAdminService.deleteRole(id);
            redirectAttributes.addFlashAttribute("flashMessage", "角色已删除");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private Set<String> permissions() {
        String username = currentUserService.getCurrentUsername();
        return username == null ? Set.of() : permissionCheckService.getPermissions(username);
    }

    private boolean isAdmin() {
        return userAdminService.isAdmin(currentUserService.getCurrentUsername());
    }

    private boolean canViewUsers() {
        if (isAdmin()) {
            return true;
        }
        Set<String> codes = permissions();
        return codes.contains("user:menus") || codes.contains("system:user:management")
                || codes.contains("role:menus") || codes.contains("system:role:management");
    }

    private boolean canViewRoleSection() {
        if (isAdmin()) {
            return true;
        }
        Set<String> codes = permissions();
        return codes.contains("role:menus") || codes.contains("system:role:management");
    }

    private boolean canManageUsers() {
        return isAdmin() || permissions().contains("system:user:management");
    }

    private boolean canManageRoles() {
        return isAdmin() || permissions().contains("system:role:management");
    }

    private Map<String, List<Permission>> groupPermissions(List<Permission> permissions) {
        Map<String, List<Permission>> groups = new LinkedHashMap<>();
        for (Permission permission : permissions) {
            String module = permission.getModule() == null || permission.getModule().isBlank()
                    ? "other" : permission.getModule();
            if ("menu".equals(module)) {
                module = "menus";
            }
            groups.computeIfAbsent(module, key -> new java.util.ArrayList<>()).add(permission);
        }
        return groups;
    }

    private Map<String, String> moduleLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("book", "图书");
        labels.put("user", "用户");
        labels.put("role", "角色");
        labels.put("menus", "菜单");
        labels.put("menu", "菜单");
        labels.put("attachment", "附件");
        labels.put("config", "配置");
        labels.put("content", "内容");
        labels.put("update", "更新");
        labels.put("settings", "设置");
        labels.put("other", "其他");
        return labels;
    }
}
