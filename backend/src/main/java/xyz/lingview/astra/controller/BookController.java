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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.domain.BookBookmark;
import xyz.lingview.astra.dto.request.BookProgressDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.domain.BookNote;
import xyz.lingview.astra.dto.response.BookStatsDTO;
import xyz.lingview.astra.dto.response.BookViewDTO;
import xyz.lingview.astra.dto.response.ChapterSearchHitDTO;
import xyz.lingview.astra.dto.response.ChapterSummaryDTO;
import xyz.lingview.astra.dto.response.PersonalStatsDTO;
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
                       @RequestParam(required = false) String shelf,
                       @RequestParam(required = false) String tag,
                       @RequestParam(defaultValue = "updated") String sort,
                       @RequestParam(defaultValue = "1") int page,
                       HttpSession session, Model model) {
        String userUuid = currentUserService.getCurrentUserUuid();
        PageResult<BookViewDTO> pageResult = bookService.listBooks(
                keyword, category_id, shelf, tag, sort, page, 12, userUuid);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category_id", category_id);
        model.addAttribute("shelf", shelf);
        model.addAttribute("tag", tag);
        model.addAttribute("tags", bookService.listTags());
        model.addAttribute("sort", sort);
        model.addAttribute("categories", bookService.listCategories());
        if (userUuid != null) {
            model.addAttribute("recentBooks", bookService.listRecentReading(userUuid, 2));
            model.addAttribute("overview", bookStatsService.getPersonalOverview(userUuid));
        }
        model.addAttribute("canManage", canManage());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "books/list";
    }

    // 独立搜索页（移动端入口）
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Integer category_id,
                         @RequestParam(required = false) String tag,
                         @RequestParam(required = false) String shelf,
                         @RequestParam(defaultValue = "updated") String sort,
                         @RequestParam(defaultValue = "1") int page,
                         HttpSession session, Model model) {
        String userUuid = currentUserService.getCurrentUserUuid();
        PageResult<BookViewDTO> pageResult = bookService.listBooks(
                q, category_id, shelf, tag, sort, page, 20, userUuid);
        boolean filtered = (q != null && !q.trim().isEmpty())
                || category_id != null
                || (tag != null && !tag.trim().isEmpty())
                || (shelf != null && !shelf.trim().isEmpty());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("q", q);
        model.addAttribute("category_id", category_id);
        model.addAttribute("tag", tag);
        model.addAttribute("shelf", shelf);
        model.addAttribute("sort", sort);
        model.addAttribute("filtered", filtered);
        model.addAttribute("categories", bookService.listCategories());
        model.addAttribute("tags", bookService.listTags());
        model.addAttribute("canManage", canManage());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "books/search";
    }

    // 随便看看
    @GetMapping("/random")
    public String randomBook() {
        Integer id = bookService.randomBookId();
        return id == null ? "redirect:/books" : "redirect:/books/" + id;
    }

    // 保存我的阅读信息（状态/评分/短评）
    @PostMapping("/{id}/my-reading")
    public String saveMyReading(@PathVariable Integer id,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) Integer rating,
                                @RequestParam(required = false) String review,
                                RedirectAttributes redirectAttributes) {
        String userUuid = currentUserService.getCurrentUserUuid();
        if (userUuid == null) {
            return "redirect:/login";
        }
        try {
            bookService.saveMyReading(id, status, rating, review, userUuid);
            redirectAttributes.addFlashAttribute("flashMessage", "阅读信息已保存");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/books/" + id;
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
                       @RequestParam(required = false) BigDecimal scroll,
                       @RequestParam(required = false) Integer note,
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
        BigDecimal restore = bookService.touchProgress(id, current.getId(), userUuid);
        if (scroll != null && scroll.compareTo(BigDecimal.ZERO) >= 0) {
            restore = scroll;
        }
        model.addAttribute("restorePercent", restore);
        List<BookBookmark> bookmarks = userUuid == null
                ? List.of() : bookService.listBookmarks(userUuid, id);
        BookBookmark currentBookmark = null;
        for (BookBookmark bookmark : bookmarks) {
            if (current.getId().equals(bookmark.getChapter_id())) {
                currentBookmark = bookmark;
                break;
            }
        }
        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("currentBookmark", currentBookmark);
        List<BookNote> notes = userUuid == null ? List.of() : bookService.listNotes(userUuid, id);
        model.addAttribute("notes", notes);
        BookNote focusNote = null;
        if (note != null) {
            for (BookNote item : notes) {
                if (note.equals(item.getId())) {
                    focusNote = item;
                    break;
                }
            }
        }
        model.addAttribute("focusNote", focusNote);
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

    // 添加书签
    @PostMapping("/{id}/bookmarks")
    @ResponseBody
    public ApiResponse<Integer> addBookmark(@PathVariable Integer id,
                                            @RequestParam Integer chapter_id,
                                            @RequestParam(defaultValue = "0") BigDecimal scroll_percent,
                                            @RequestParam(required = false) String note) {
        String userUuid = currentUserService.getCurrentUserUuid();
        if (userUuid == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        try {
            return ApiResponse.success(bookService.addBookmark(id, chapter_id, scroll_percent, note, userUuid));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 删除书签
    @PostMapping("/{id}/bookmarks/{bookmarkId}/delete")
    @ResponseBody
    public ApiResponse<String> deleteBookmark(@PathVariable Integer id, @PathVariable Integer bookmarkId) {
        String userUuid = currentUserService.getCurrentUserUuid();
        if (userUuid == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        bookService.deleteBookmark(bookmarkId, userUuid);
        return ApiResponse.success("ok");
    }

    // 添加笔记
    @PostMapping("/{id}/notes")
    @ResponseBody
    public ApiResponse<String> addNote(@PathVariable Integer id,
                                       @RequestParam Integer chapter_id,
                                       @RequestParam(required = false) String quote_text,
                                       @RequestParam(required = false) String note_text) {
        String userUuid = currentUserService.getCurrentUserUuid();
        if (userUuid == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        try {
            bookService.addNote(id, chapter_id, quote_text, note_text, userUuid);
            return ApiResponse.success("ok");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 删除笔记
    @PostMapping("/{id}/notes/{noteId}/delete")
    @ResponseBody
    public ApiResponse<String> deleteNote(@PathVariable Integer id, @PathVariable Integer noteId) {
        String userUuid = currentUserService.getCurrentUserUuid();
        if (userUuid == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        bookService.deleteNote(noteId, userUuid);
        return ApiResponse.success("ok");
    }

    // 导出笔记
    @GetMapping("/{id}/notes/export")
    public void exportNotes(@PathVariable Integer id, HttpServletResponse response) throws java.io.IOException {
        String userUuid = currentUserService.getCurrentUserUuid();
        List<BookNote> notes = bookService.listNotes(userUuid, id);
        StringBuilder sb = new StringBuilder("# 阅读笔记\n\n");
        for (BookNote note : notes) {
            sb.append("## ").append(note.getChapter_title() == null ? "未知章节" : note.getChapter_title())
                    .append("\n\n");
            if (note.getQuote_text() != null) {
                sb.append("> ").append(note.getQuote_text().replace("\n", " ")).append("\n\n");
            }
            if (note.getNote_text() != null) {
                sb.append(note.getNote_text()).append("\n\n");
            }
        }
        response.setContentType("text/markdown;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=book-notes-" + id + ".md");
        response.getWriter().write(sb.toString());
    }

    // 全文搜索页
    @GetMapping("/fulltext")
    public String fulltext(@RequestParam(required = false) String q, HttpSession session, Model model) {
        model.addAttribute("q", q);
        model.addAttribute("results", bookService.searchAll(q));
        model.addAttribute("canManage", canManage());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "books/fulltext";
    }

    // 书内搜索
    @GetMapping("/{id}/search")
    @ResponseBody
    public ApiResponse<List<ChapterSearchHitDTO>> searchInBook(@PathVariable Integer id,
                                                               @RequestParam String q) {
        return ApiResponse.success(bookService.searchInBook(id, q));
    }

    // 个人阅读统计
    @GetMapping("/stats/personal")
    @ResponseBody
    public ApiResponse<PersonalStatsDTO> personalStats(@RequestParam(required = false) Integer year) {
        String userUuid = currentUserService.getCurrentUserUuid();
        int targetYear = year == null ? java.time.LocalDate.now().getYear() : year;
        return ApiResponse.success(bookStatsService.getPersonalStats(userUuid, targetYear));
    }

    // 保存年度阅读目标
    @PostMapping("/goal")
    @ResponseBody
    public ApiResponse<String> saveGoal(@RequestParam int year, @RequestParam int target) {
        String userUuid = currentUserService.getCurrentUserUuid();
        if (userUuid == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        try {
            bookStatsService.saveGoal(userUuid, year, target);
            return ApiResponse.success("ok");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
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