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
import xyz.lingview.astra.dto.response.CoverCandidateDTO;
import xyz.lingview.astra.dto.response.ImportPreviewDTO;
import xyz.lingview.astra.dto.response.ParsedChapterDTO;
import xyz.lingview.astra.dto.response.PreviewChapterContentDTO;
import xyz.lingview.astra.security.CsrfTokenService;
import xyz.lingview.astra.service.BookChapterService;
import xyz.lingview.astra.service.BookService;
import xyz.lingview.astra.service.CoverSearchService;
import xyz.lingview.astra.service.ImportPreviewService;
import xyz.lingview.astra.service.UploadService;

import java.util.LinkedHashMap;
import java.util.List;
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
    private CoverSearchService coverSearchService;

    @Autowired
    private ImportPreviewService importPreviewService;

    @Autowired
    private CsrfTokenService csrfTokenService;

    // 新增图书表单
    @GetMapping("/new")
    @RequiresPermission(MANAGE_PERMISSION)
    public String newBookForm(HttpSession session, Model model) {
        return renderBookForm(model, session, null, null, new BookFormDTO(), null, null);
    }

    // 新增图书（可同时导入正文文件）
    @PostMapping
    @RequiresPermission(MANAGE_PERMISSION)
    public String createBook(@Valid @ModelAttribute("bookForm") BookFormDTO form, BindingResult binding,
                             @RequestParam(value = "cover", required = false) MultipartFile cover,
                             @RequestParam(value = "bookFile", required = false) MultipartFile bookFile,
                             @RequestParam(value = "importToken", required = false) String importToken,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request, HttpSession session, Model model) {
        if (binding.hasErrors()) {
            addImportPreview(model, importToken, session);
            return renderBookForm(model, session, null, null, form, fieldErrors(binding), null);
        }

        List<ParsedChapterDTO> chapters;
        try {
            chapters = resolveChapters(bookFile, importToken, session);
        } catch (RuntimeException | java.io.IOException e) {
            addImportPreview(model, importToken, session);
            String message = e instanceof java.io.IOException ? "读取文件失败" : e.getMessage();
            return renderBookForm(model, session, null, null, form, null, message);
        }

        try {
            String coverUrl = resolveCover(request, cover, form.getCover_url());
            Integer id = chapters == null
                    ? bookService.createBook(form, coverUrl)
                    : bookService.createBookWithChapters(form, coverUrl, chapters);
            if (importToken != null && !importToken.trim().isEmpty()) {
                importPreviewService.discard(importToken.trim(), session);
            }
            if (chapters != null && !chapters.isEmpty()) {
                redirectAttributes.addFlashAttribute("flashMessage",
                        "图书已创建，共导入 " + chapters.size() + " 章");
            }
            return "redirect:/books/" + id;
        } catch (RuntimeException e) {
            addImportPreview(model, importToken, session);
            return renderBookForm(model, session, null, null, form, null, e.getMessage());
        }
    }

    private List<ParsedChapterDTO> resolveChapters(MultipartFile bookFile, String importToken,
                                                    HttpSession session) throws java.io.IOException {
        if (importToken != null && !importToken.trim().isEmpty()) {
            return importPreviewService.loadChapters(importToken.trim(), session);
        }
        if (bookFile != null && !bookFile.isEmpty()) {
            return bookChapterService.parseChapters(bookFile.getOriginalFilename(), bookFile.getBytes());
        }
        return null;
    }

    private void addImportPreview(Model model, String importToken, HttpSession session) {
        if (importToken == null || importToken.trim().isEmpty()) {
            return;
        }
        try {
            model.addAttribute("importPreview", importPreviewService.getPreview(importToken.trim(), session));
            model.addAttribute("importToken", importToken.trim());
        } catch (RuntimeException ignored) {
        }
    }

    // 上传文件并生成预览（不落库）
    @PostMapping("/import/preview")
    @RequiresPermission(MANAGE_PERMISSION)
    @ResponseBody
    public ApiResponse<ImportPreviewDTO> createImportPreview(@RequestParam("file") MultipartFile file,
                                                             HttpSession session) {
        try {
            return ApiResponse.success(importPreviewService.createPreview(file, session));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 获取预览摘要与章节列表
    @GetMapping("/import/preview/{token}")
    @RequiresPermission(MANAGE_PERMISSION)
    @ResponseBody
    public ApiResponse<ImportPreviewDTO> getImportPreview(@PathVariable String token, HttpSession session) {
        try {
            return ApiResponse.success(importPreviewService.getPreview(token, session));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 获取预览中某章正文
    @GetMapping("/import/preview/{token}/chapters/{index}")
    @RequiresPermission(MANAGE_PERMISSION)
    @ResponseBody
    public ApiResponse<PreviewChapterContentDTO> getImportChapter(@PathVariable String token,
                                                                  @PathVariable int index,
                                                                  HttpSession session) {
        try {
            return ApiResponse.success(importPreviewService.getChapter(token, session, index));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 保存预览中某章的标题与正文
    @PostMapping("/import/preview/{token}/chapters/{index}")
    @RequiresPermission(MANAGE_PERMISSION)
    @ResponseBody
    public ApiResponse<ImportPreviewDTO> saveImportChapter(@PathVariable String token,
                                                           @PathVariable int index,
                                                           @RequestParam(required = false) String title,
                                                           @RequestParam(required = false) String content,
                                                           HttpSession session) {
        try {
            return ApiResponse.success(importPreviewService.saveChapter(token, session, index, title, content));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 删除预览中的某章
    @PostMapping("/import/preview/{token}/chapters/{index}/delete")
    @RequiresPermission(MANAGE_PERMISSION)
    @ResponseBody
    public ApiResponse<ImportPreviewDTO> deleteImportChapter(@PathVariable String token,
                                                             @PathVariable int index,
                                                             HttpSession session) {
        try {
            return ApiResponse.success(importPreviewService.deleteChapter(token, session, index));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 放弃预览文件
    @PostMapping("/import/preview/{token}/discard")
    @RequiresPermission(MANAGE_PERMISSION)
    @ResponseBody
    public ApiResponse<String> discardImportPreview(@PathVariable String token, HttpSession session) {
        importPreviewService.discard(token, session);
        return ApiResponse.success("ok");
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
        form.setIsbn(book.getIsbn());
        form.setPublisher(book.getPublisher());
        form.setPublish_year(book.getPublish_year());
        form.setSource_url(book.getSource_url());
        form.setTags(book.getTag_names());
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
    public String chapterList(@PathVariable Integer id,
                              @RequestParam(defaultValue = "1") int page,
                              HttpSession session, Model model, HttpServletResponse response) {
        BookViewDTO book = bookService.getBook(id, null);
        if (book == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("book", book);
        model.addAttribute("pageResult", bookChapterService.listChapterPage(id, page, 20));
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

    // 搜索封面候选图
    @GetMapping("/cover-search")
    @RequiresPermission(MANAGE_PERMISSION)
    @ResponseBody
    public ApiResponse<List<CoverCandidateDTO>> coverSearch(@RequestParam String keyword,
                                                            @RequestParam(defaultValue = "12") int limit) {
        return ApiResponse.success(coverSearchService.search(keyword, limit));
    }

    // 下载选中的封面图并入附件库
    @PostMapping("/cover-download")
    @RequiresPermission(MANAGE_PERMISSION)
    @ResponseBody
    public ApiResponse<Map<String, String>> coverDownload(@RequestParam String url, HttpServletRequest request) {
        return uploadService.storeImageFromUrl(request, url);
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

    // 分类改名/调整排序
    @PostMapping("/categories/{categoryId}/edit")
    @RequiresPermission(MANAGE_PERMISSION)
    public String renameCategory(@PathVariable Integer categoryId,
                                 @RequestParam String name,
                                 @RequestParam(required = false) Integer sort_order,
                                 RedirectAttributes redirectAttributes) {
        try {
            bookService.renameCategory(categoryId, name, sort_order);
            redirectAttributes.addFlashAttribute("flashMessage", "分类已更新");
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