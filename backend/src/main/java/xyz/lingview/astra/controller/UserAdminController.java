package xyz.lingview.astra.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.lingview.astra.security.CsrfTokenService;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.UserAdminService;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private CsrfTokenService csrfTokenService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page, HttpSession session, Model model,
                       HttpServletResponse response) {
        if (!isAdmin()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "error/403";
        }
        model.addAttribute("pageResult", userAdminService.listUsers(page, 20));
        model.addAttribute("roles", userAdminService.listRoles());
        model.addAttribute("canManage", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "admin/users";
    }

    @PostMapping("/{id}/roles")
    public String updateRoles(@PathVariable Integer id,
                              @RequestParam(required = false) List<Integer> roleIds,
                              RedirectAttributes redirectAttributes,
                              HttpServletResponse response) {
        if (!isAdmin()) {
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

    private boolean isAdmin() {
        return userAdminService.isAdmin(currentUserService.getCurrentUsername());
    }
}
