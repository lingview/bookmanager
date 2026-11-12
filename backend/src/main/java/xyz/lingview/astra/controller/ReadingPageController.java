package xyz.lingview.astra.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.lingview.astra.security.CsrfTokenService;
import xyz.lingview.astra.service.BookService;
import xyz.lingview.astra.service.BookStatsService;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.UserPermissionCheckService;

@Controller
public class ReadingPageController {

    private static final String MANAGE_PERMISSION = "book:management";

    @Autowired
    private BookService bookService;

    @Autowired
    private BookStatsService bookStatsService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private UserPermissionCheckService permissionCheckService;

    @Autowired
    private CsrfTokenService csrfTokenService;

    @GetMapping("/reading")
    public String reading(HttpSession session, Model model) {
        String userUuid = currentUserService.getCurrentUserUuid();
        if (userUuid != null) {
            model.addAttribute("recentBooks", bookService.listRecentReading(userUuid, 3));
            model.addAttribute("overview", bookStatsService.getPersonalOverview(userUuid));
        }
        model.addAttribute("canManage", canManage());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "reading";
    }

    private boolean canManage() {
        String username = currentUserService.getCurrentUsername();
        return username != null
                && permissionCheckService.hasAnyPermission(username, new String[]{MANAGE_PERMISSION});
    }
}
