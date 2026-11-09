package xyz.lingview.astra.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.lingview.astra.dto.request.LoginDTO;
import xyz.lingview.astra.security.CsrfTokenService;
import xyz.lingview.astra.service.CaptchaService;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.LoginService;
import xyz.lingview.astra.util.UrlUtil;

import java.util.Map;

@Slf4j
@Controller
public class PageController {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private CsrfTokenService csrfTokenService;

    @Autowired
    private CurrentUserService currentUserService;

    // 渲染登录页
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect, HttpSession session, Model model) {
        model.addAttribute("redirect", UrlUtil.sanitizeInternalRedirect(redirect));
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        prepareCaptcha(session, model);
        return "login";
    }

    // 提交登录表单
    @PostMapping("/login")
    public String login(@ModelAttribute LoginDTO loginDTO,
                        @RequestParam(required = false) String redirect,
                        @RequestParam(required = false) String csrfToken,
                        HttpServletRequest request,
                        Model model) {
        String target = UrlUtil.sanitizeInternalRedirect(redirect);

        if (!csrfTokenService.isValid(request.getSession(), csrfToken)) {
            log.warn("登录表单 CSRF 校验失败");
            return renderLoginFailure(request, model, target, loginDTO.getUsername(), "页面已过期，请重新提交");
        }

        try {
            loginService.login(loginDTO, request);
        } catch (RuntimeException e) {
            log.warn("页面登录失败: {}", e.getMessage());
            return renderLoginFailure(request, model, target, loginDTO.getUsername(), e.getMessage());
        }

        return "redirect:" + target;
    }

    // 提交登出表单
    @PostMapping("/logout")
    public String logout(@RequestParam(required = false) String csrfToken, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && csrfTokenService.isValid(session, csrfToken)) {
            loginService.logout((String) session.getAttribute("username"), session);
        }
        return "redirect:/login";
    }

    // 渲染首页
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        model.addAttribute("username", currentUserService.getCurrentUsername());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "home";
    }

    private String renderLoginFailure(HttpServletRequest request, Model model, String target,
                                      String username, String message) {
        HttpSession session = request.getSession();
        model.addAttribute("redirect", target);
        model.addAttribute("username", username);
        model.addAttribute("error", message);
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        prepareCaptcha(session, model);
        return "login";
    }

    private void prepareCaptcha(HttpSession session, Model model) {
        Map<String, String> captcha = captchaService.generateCaptcha(session);
        model.addAttribute("captchaImage", captcha.get("image"));
        model.addAttribute("captchaKey", captcha.get("key"));
    }
}