package xyz.lingview.astra.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.lingview.astra.annotation.RequiresPermission;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.dto.request.BookCategoryFormDTO;
import xyz.lingview.astra.dto.request.BookChapterFormDTO;
import xyz.lingview.astra.dto.request.BookFormDTO;
import xyz.lingview.astra.dto.request.ChapterOrderDTO;
import xyz.lingview.astra.dto.response.BookViewDTO;
import xyz.lingview.astra.security.CsrfTokenService;
import xyz.lingview.astra.service.BookChapterService;
import xyz.lingview.astra.service.BookService;
import xyz.lingview.astra.service.UploadService;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/books")
public class BookAdminController {

    private static final String MANAGE_PERMISSION = "book:management";

    @Autowired
    private BookService bookService;

    @Autowired
    private BookChapterService bookChapterService;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private CsrfTokenService csrfTokenService;

    // 新增图书表单
    @GetMapping("/new")
    @RequiresPermission(MANAGE_PERMISSION)
    public String newBookForm(HttpSession session, Model model) {
        return renderBookForm(model, session, null, null, new BookFormDTO(), null, null);
    }

    // 新增图书
    @PostMapping
    @RequiresPermission(MANAGE_PERMISSION)
    public String createBook(@Valid @ModelAttribute("bookForm") BookFormDTO form, BindingResult binding,
                             @RequestParam(value = "cover", required = false) MultipartFile cover,
                             HttpServletRequest request, HttpSession session, Model model) {
        if (binding.hasErrors()) {
            return renderBookForm(model, session, null, null, form, fieldErrors(binding), null);
        }
        try {
            Integer id = bookService.createBook(form, resolveCover(request, cover, null));
            return "redirect:/books/" + id;
        } catch (RuntimeException e) {
            return renderBookForm(model, session, null, null, form, null, e.getMessage());
        }
    }

    // 编辑图书表单
    @GetMapping("/{id}/edit")
    @RequiresPermission(MANAGE_PERMISSION)
    public String editBookForm(@PathVariable Integer id, HttpSession session, Model model,
                               HttpServletResponse response) {
        BookViewDTO book = bookService.getBook(id, null);
        if (book == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        BookFormDTO form = new BookFormDTO();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setCategory_id(book.getCategory_id());
        form.setDescription(book.getDescription());
        form.setCover_url(book.getCover_url());
        return renderBookForm(model, session, id, book.getCover_url(), form, null, null);
    }

    // 保存图书修改
    @PostMapping("/{id}/edit")
    @RequiresPermission(MANAGE_PERMISSION)
    public String updateBook(@PathVariable Integer id, @Valid @ModelAttribute("bookForm") BookFormDTO form,
                             BindingResult binding,
                             @RequestParam(value = "cover", required = false) MultipartFile cover,
                             HttpServletRequest request, HttpSession session, Model model) {
        String existingCover = form.getCover_url();
        if (binding.hasErrors()) {
            return renderBookForm(model, session, id, existingCover, form, fieldErrors(binding), null);
        }
        try {
            bookService.updateBook(id, form, resolveCover(request, cover, existingCover));
            return "redirect:/books/" + id;
        } catch (RuntimeException e) {
            return renderBookForm(model, session, id, existingCover, form, null, e.getMessage());
        }
    }

    // 删除图书
    @PostMapping("/{id}/delete")
    @RequiresPermission(MANAGE_PERMISSION)
    public String deleteBook(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("flashMessage", "图书已删除");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/books";
    }

    // 章节管理页
    @GetMapping("/{id}/chapters")
    @RequiresPermission(MANAGE_PERMISSION)
    public String chapterList(@PathVariable Integer id, HttpSession session, Model model,
                              HttpServletResponse response) {
        BookViewDTO book = bookService.getBook(id, null);
        if (book == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("book", book);
        model.addAttribute("chapters", bookChapterService.listChapters(id));
        model.addAttribute("canManage", true);
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "books/chapters";
    }

    // 新增章节表单
    @GetMapping("/{id}/chapters/new")
    @RequiresPermission(MANAGE_PERMISSION)
    public String newChapterForm(@PathVariable Integer id, HttpSession session, Model model,
                                 HttpServletResponse response) {
        if (bookService.getBook(id, null) == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        return renderChapterForm(model, session, id, null, new BookChapterFormDTO(), null, null);
    }

    // 新增章节
    @PostMapping("/{id}/chapters")
    @RequiresPermission(MANAGE_PERMISSION)
    public String createChapter(@PathVariable Integer id,
                                @Valid @ModelAttribute("chapterForm") BookChapterFormDTO form,
                                BindingResult binding, HttpSession session, Model model) {
        if (binding.hasErrors()) {
            return renderChapterForm(model, session, id, null, form, fieldErrors(binding), null);
        }
        try {
            bookChapterService.createChapter(id, form);
            return "redirect:/books/" + id + "/chapters";
        } catch (RuntimeException e) {
            return renderChapterForm(model, session, id, null, form, null, e.getMessage());
        }
    }

    // 编辑章节表单
    @GetMapping("/{id}/chapters/{chapterId}/edit")
    @RequiresPermission(MANAGE_PERMISSION)
    public String editChapterForm(@PathVariable Integer id, @PathVariable Integer chapterId,
                                  HttpSession session, Model model, HttpServletResponse response) {
        BookChapter chapter = bookChapterService.getChapter(id, chapterId);
        if (chapter == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        BookChapterFormDTO form = new BookChapterFormDTO();
        form.setTitle(chapter.getTitle());
        form.setContent(chapter.getContent());
        return renderChapterForm(model, session, id, chapterId, form, null, null);
    }

    // 保存章节修改
    @PostMapping("/{id}/chapters/{chapterId}/edit")
    @RequiresPermission(MANAGE_PERMISSION)
    public String updateChapter(@PathVariable Integer id, @PathVariable Integer chapterId,
                                @Valid @ModelAttribute("chapterForm") BookChapterFormDTO form,
                                BindingResult binding, HttpSession session, Model model) {
        if (binding.hasErrors()) {
            return renderChapterForm(model, session, id, chapterId, form, fieldErrors(binding), null);
        }
        try {
            bookChapterService.updateChapter(id, chapterId, form);
            return "redirect:/books/" + id + "/chapters";
        } catch (RuntimeException e) {
            return renderChapterForm(model, session, id, chapterId, form, null, e.getMessage());
        }
    }

    // 删除章节
    @PostMapping("/{id}/chapters/{chapterId}/delete")
    @RequiresPermission(MANAGE_PERMISSION)
    public String deleteChapter(@PathVariable Integer id, @PathVariable Integer chapterId,
                                RedirectAttributes redirectAttributes) {
        try {
            bookChapterService.deleteChapter(id, chapterId);
            redirectAttributes.addFlashAttribute("flashMessage", "章节已删除");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/books/" + id + "/chapters";
    }

    // 章节拖拽排序
    @PostMapping("/{id}/chapters/reorder")
    @RequiresPermission(MANAGE_PERMISSION)
    @ResponseBody
    public ApiResponse<String> reorderChapters(@PathVariable Integer id, @RequestBody @Valid ChapterOrderDTO dto) {
        try {
            bookChapterService.reorder(id, dto.getIds());
            return ApiResponse.success("ok");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 批量导入章节
    @PostMapping("/{id}/chapters/import")
    @RequiresPermission(MANAGE_PERMISSION)
    public String importChapters(@PathVariable Integer id,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            int count = bookChapterService.importChapters(id, file);
            redirectAttributes.addFlashAttribute("flashMessage", "成功导入 " + count + " 章");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/books/" + id + "/chapters";
    }

    // 分类管理页
    @GetMapping("/categories")
    @RequiresPermission(MANAGE_PERMISSION)
    public String categoryList(HttpSession session, Model model) {
        model.addAttribute("categories", bookService.listCategories());
        model.addAttribute("categoryForm", new BookCategoryFormDTO());
        model.addAttribute("canManage", true);
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        return "books/categories";
    }

    // 新增分类
    @PostMapping("/categories")
    @RequiresPermission(MANAGE_PERMISSION)
    public String createCategory(@Valid @ModelAttribute("categoryForm") BookCategoryFormDTO form,
                                 BindingResult binding, HttpSession session, Model model,
                                 RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            model.addAttribute("categories", bookService.listCategories());
            model.addAttribute("csrfToken", csrfTokenService.getToken(session));
            model.addAttribute("canManage", true);
            model.addAttribute("errors", fieldErrors(binding));
            return "books/categories";
        }
        try {
            bookService.createCategory(form);
            redirectAttributes.addFlashAttribute("flashMessage", "分类已添加");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/books/categories";
    }

    // 删除分类
    @PostMapping("/categories/{categoryId}/delete")
    @RequiresPermission(MANAGE_PERMISSION)
    public String deleteCategory(@PathVariable Integer categoryId, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteCategory(categoryId);
            redirectAttributes.addFlashAttribute("flashMessage", "分类已删除");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/books/categories";
    }

    private String renderBookForm(Model model, HttpSession session, Integer bookId, String coverUrl,
                                  BookFormDTO form, Map<String, String> errors, String error) {
        model.addAttribute("bookForm", form);
        model.addAttribute("bookId", bookId);
        model.addAttribute("coverUrl", coverUrl);
        model.addAttribute("canManage", true);
        model.addAttribute("categories", bookService.listCategories());
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        if (errors != null) {
            model.addAttribute("errors", errors);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "books/form";
    }

    private String renderChapterForm(Model model, HttpSession session, Integer bookId, Integer chapterId,
                                     BookChapterFormDTO form, Map<String, String> errors, String error) {
        model.addAttribute("book", bookService.getBook(bookId, null));
        model.addAttribute("bookId", bookId);
        model.addAttribute("chapterId", chapterId);
        model.addAttribute("chapterForm", form);
        model.addAttribute("canManage", true);
        model.addAttribute("csrfToken", csrfTokenService.getToken(session));
        if (errors != null) {
            model.addAttribute("errors", errors);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "books/chapter-form";
    }

    private Map<String, String> fieldErrors(BindingResult binding) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : binding.getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }

    private String resolveCover(HttpServletRequest request, MultipartFile cover, String existingCover) {
        if (cover == null || cover.isEmpty()) {
            return existingCover;
        }
        ApiResponse<Map<String, String>> result = uploadService.uploadBookCover(request, cover);
        if (result.getCode() != 200 || result.getData() == null) {
            throw new RuntimeException(result.getMessage());
        }
        return result.getData().get("fileUrl");
    }
}