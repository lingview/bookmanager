package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.domain.Book;
import xyz.lingview.astra.domain.BookCategory;
import xyz.lingview.astra.domain.BookReadingProgress;
import xyz.lingview.astra.dto.request.BookCategoryFormDTO;
import xyz.lingview.astra.dto.request.BookFormDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.dto.response.BookViewDTO;
import xyz.lingview.astra.mapper.BookMapper;
import xyz.lingview.astra.service.BookService;
import xyz.lingview.astra.service.BookStatsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BookStatsService bookStatsService;

    @Override
    public PageResult<BookViewDTO> listBooks(String keyword, Integer categoryId, int pageNum, int pageSize,
                                             String userUuid) {
        int page = Math.max(pageNum, 1);
        int size = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        String trimmedKeyword = keyword == null ? null : keyword.trim();
        String keywordPattern = (trimmedKeyword == null || trimmedKeyword.isEmpty())
                ? null : "%" + trimmedKeyword + "%";

        int total = bookMapper.countBooks(keywordPattern, categoryId);
        List<BookViewDTO> data = bookMapper.selectBooks(keywordPattern, categoryId, userUuid,
                (page - 1) * size, size);

        PageResult<BookViewDTO> result = new PageResult<>();
        result.setData(data);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setTotal_pages((int) Math.ceil((double) total / size));
        return result;
    }

    @Override
    public BookViewDTO getBook(Integer id, String userUuid) {
        return bookMapper.selectBook(id, userUuid);
    }

    @Override
    public Integer createBook(BookFormDTO dto, String coverUrl) {
        Book book = new Book();
        book.setTitle(dto.getTitle().trim());
        book.setAuthor(trimToNull(dto.getAuthor()));
        book.setCategory_id(dto.getCategory_id());
        book.setDescription(trimToNull(dto.getDescription()));
        book.setCover_url(coverUrl);
        bookMapper.insertBook(book);
        return book.getId();
    }

    @Override
    public void updateBook(Integer id, BookFormDTO dto, String coverUrl) {
        if (bookMapper.selectBook(id, null) == null) {
            throw new RuntimeException("图书不存在或已删除");
        }
        Book book = new Book();
        book.setId(id);
        book.setTitle(dto.getTitle().trim());
        book.setAuthor(trimToNull(dto.getAuthor()));
        book.setCategory_id(dto.getCategory_id());
        book.setDescription(trimToNull(dto.getDescription()));
        book.setCover_url(coverUrl);
        book.setUpdate_time(LocalDateTime.now());
        bookMapper.updateBook(book);
    }

    @Override
    public void deleteBook(Integer id) {
        if (bookMapper.softDeleteBook(id) <= 0) {
            throw new RuntimeException("图书不存在或已删除");
        }
    }

    @Override
    public List<BookCategory> listCategories() {
        return bookMapper.selectCategories();
    }

    @Override
    public void createCategory(BookCategoryFormDTO dto) {
        String name = dto.getName().trim();
        if (bookMapper.countCategoryByName(name) > 0) {
            throw new RuntimeException("分类名称已存在");
        }
        BookCategory category = new BookCategory();
        category.setName(name);
        category.setSort_order(dto.getSort_order() == null ? 0 : dto.getSort_order());
        bookMapper.insertCategory(category);
    }

    @Override
    public void deleteCategory(Integer id) {
        int bookCount = bookMapper.countBooksByCategory(id);
        if (bookCount > 0) {
            throw new RuntimeException("该分类下还有 " + bookCount + " 本图书，无法删除");
        }
        if (bookMapper.deleteCategory(id) <= 0) {
            throw new RuntimeException("分类不存在");
        }
    }

    @Override
    public BigDecimal touchProgress(Integer bookId, Integer chapterId, String userUuid) {
        BookReadingProgress progress = bookMapper.selectProgress(userUuid, bookId);
        if (progress == null) {
            insertProgress(bookId, chapterId, BigDecimal.ZERO, userUuid);
            bookStatsService.recordChapterOpen(bookId, chapterId, userUuid);
            return BigDecimal.ZERO;
        }
        if (!chapterId.equals(progress.getChapter_id())) {
            bookMapper.updateProgressChapter(userUuid, bookId, chapterId);
            bookStatsService.recordChapterOpen(bookId, chapterId, userUuid);
            return BigDecimal.ZERO;
        }
        return progress.getScroll_percent() == null ? BigDecimal.ZERO : progress.getScroll_percent();
    }

    @Override
    public void saveProgress(Integer bookId, Integer chapterId, BigDecimal scrollPercent, String userUuid) {
        BookReadingProgress progress = bookMapper.selectProgress(userUuid, bookId);
        if (progress == null) {
            insertProgress(bookId, chapterId, scrollPercent, userUuid);
        } else {
            bookMapper.updateProgressScroll(userUuid, bookId, chapterId, scrollPercent);
        }
    }

    private void insertProgress(Integer bookId, Integer chapterId, BigDecimal scrollPercent, String userUuid) {
        BookReadingProgress created = new BookReadingProgress();
        created.setUser_uuid(userUuid);
        created.setBook_id(bookId);
        created.setChapter_id(chapterId);
        created.setScroll_percent(scrollPercent);
        try {
            bookMapper.insertProgress(created);
        } catch (DataIntegrityViolationException e) {
            bookMapper.updateProgressScroll(userUuid, bookId, chapterId, scrollPercent);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}