package xyz.lingview.astra.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.lingview.astra.service.UserAdminService;
import xyz.lingview.astra.service.UserPermissionCheckService;

import java.util.Set;

@Component
public class AdminNavInterceptor implements HandlerInterceptor {

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private UserPermissionCheckService permissionCheckService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }
        Object username = session.getAttribute("username");
        if (username == null) {
            return true;
        }
        String name = (String) username;
        boolean admin = userAdminService.isAdmin(name);
        Set<String> permissions = permissionCheckService.getPermissions(name);

        request.setAttribute("isAdmin", admin);
        request.setAttribute("showUsersMenu", admin
                || permissions.contains("user:menus")
                || permissions.contains("system:user:management")
                || permissions.contains("role:menus")
                || permissions.contains("system:role:management"));
        request.setAttribute("showCategoriesMenu", admin
                || permissions.contains("book:management")
                || permissions.contains("content:menus"));
        request.setAttribute("showRolesMenu", admin
                || permissions.contains("role:menus")
                || permissions.contains("system:role:management"));
        request.setAttribute("canManageUsers", admin
                || permissions.contains("system:user:management"));
        request.setAttribute("canManageRoles", admin
                || permissions.contains("system:role:management"));
        return true;
    }
}
