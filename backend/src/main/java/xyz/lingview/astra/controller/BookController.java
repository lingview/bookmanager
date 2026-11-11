package xyz.lingview.astra.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.dto.request.BookProgressDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.dto.response.BookStatsDTO;
import xyz.lingview.astra.dto.response.BookViewDTO;
import xyz.lingview.astra.dto.response.ChapterSummaryDTO;
import xyz.lingview.astra.security.CsrfTokenService;
import xyz.lingview.astra.service.BookChapterService;
import xyz.lingview.astra.service.BookService;
import xyz.lingview.astra.service.BookStatsService;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.UserPermissionCheckService;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/books")
public class BookController {

    private static final String MANAGE_PERMISSION = "book:management";

    @Autowired
    private BookService bookService;

    @Autowired
    private BookChapterService bookChapterService;

    @Autowired
    private BookStatsService bookStatsService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private UserPermissionCheckService permissionCheckService;

    @Autowired
    private CsrfTokenService csrfTokenService;

    // 图书列表
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer category_id,
                       @RequestParam(defaultValue = "1") int page,
                       HttpSession session, Model model) {
        PageResult<BookViewDTO> pageResult = bookService.listBooks(
                keyword, category_id, page, 12, currentUserService.getCurrentUserUuid());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category_id", category_id);
        model.addAttribute("categories", bookService.listCategories());
        model.addAttribute("canManage", canManage());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "books/list";
    }

    // 图书详情
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, HttpSession session, Model model,
                         HttpServletResponse response) {
        BookViewDTO book = bookService.getBook(id, currentUserService.getCurrentUserUuid());
        if (book == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("book", book);
        model.addAttribute("toc", bookChapterService.listAllChapters(id));
        model.addAttribute("canManage", canManage());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "books/detail";
    }

    // 阅读章节
    @GetMapping("/{id}/read")
    public String read(@PathVariable Integer id,
                       @RequestParam(required = false) Integer chapterId,
                       HttpSession session, Model model, HttpServletResponse response) {
        String userUuid = currentUserService.getCurrentUserUuid();
        BookViewDTO book = bookService.getBook(id, userUuid);
        if (book == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }

        List<ChapterSummaryDTO> toc = bookChapterService.listAllChapters(id);
        ChapterSummaryDTO current = null;
        if (chapterId != null) {
            for (ChapterSummaryDTO summary : toc) {
                if (summary.getId().equals(chapterId)) {
                    current = summary;
                    break;
                }
            }
        }
        if (current == null && !toc.isEmpty()) {
            current = toc.get(0);
        }

        BookChapter chapter = current == null ? null : bookChapterService.getChapter(id, current.getId());
        if (chapter == null) {
            model.addAttribute("book", book);
            model.addAttribute("toc", toc);
            model.addAttribute("noChapter", true);
            model.addAttribute("canManage", canManage());
            model.addAttribute("csrfToken", csrfTokenService.getToken(session));
            return "books/read";
        }

        int index = toc.indexOf(current);
        model.addAttribute("book", book);
        model.addAttribute("toc", toc);
        model.addAttribute("chapter", chapter);
        model.addAttribute("prevChapter", index > 0 ? toc.get(index - 1) : null);
        model.addAttribute("nextChapter", index < toc.size() - 1 ? toc.get(index + 1) : null);
        model.addAttribute("restorePercent", bookService.touchProgress(id, current.getId(), userUuid));
        model.addAttribute("canManage", canManage());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "books/read";
    }

    // 保存阅读进度
    @PostMapping("/{id}/progress")
    @ResponseBody
    public ApiResponse<String> saveProgress(@PathVariable Integer id,
                                            @ModelAttribute @Valid BookProgressDTO dto,
                                            BindingResult binding) {
        if (binding.hasErrors()) {
            return ApiResponse.error(400, binding.getAllErrors().get(0).getDefaultMessage());
        }
        String userUuid = currentUserService.getCurrentUserUuid();
        if (userUuid == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        if (bookChapterService.getChapter(id, dto.getChapter_id()) == null) {
            return ApiResponse.error(400, "章节不存在或不属于该图书");
        }
        BigDecimal percent = dto.getScroll_percent() == null ? BigDecimal.ZERO : dto.getScroll_percent();
        bookService.saveProgress(id, dto.getChapter_id(), percent, userUuid);
        return ApiResponse.success("ok");
    }

    // 图书统计
    @GetMapping("/stats")
    public String stats(HttpSession session, Model model) {
        model.addAttribute("canManage", canManage());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "books/stats";
    }

    // 图书统计数据
    @GetMapping("/stats/data")
    @ResponseBody
    public ApiResponse<BookStatsDTO> statsData() {
        return ApiResponse.success(bookStatsService.getStats());
    }

    // 阅读心跳
    @PostMapping("/{id}/reading-time")
    @ResponseBody
    public ApiResponse<String> recordReadingTime(@PathVariable Integer id,
                                                 @RequestParam(defaultValue = "0") int seconds) {
        String userUuid = currentUserService.getCurrentUserUuid();
        if (userUuid == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        bookStatsService.recordHeartbeat(id, seconds, userUuid);
        return ApiResponse.success("ok");
    }

    private boolean canManage() {
        String username = currentUserService.getCurrentUsername();
        return username != null
                && permissionCheckService.hasAnyPermission(username, new String[]{MANAGE_PERMISSION});
    }
}