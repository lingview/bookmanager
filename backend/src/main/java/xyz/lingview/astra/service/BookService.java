package xyz.lingview.astra.service;

import xyz.lingview.astra.domain.BookCategory;
import xyz.lingview.astra.dto.request.BookCategoryFormDTO;
import xyz.lingview.astra.dto.request.BookFormDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.dto.response.BookViewDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {

    PageResult<BookViewDTO> listBooks(String keyword, Integer categoryId, int pageNum, int pageSize, String userUuid);

    BookViewDTO getBook(Integer id, String userUuid);

    Integer createBook(BookFormDTO dto, String coverUrl);

    void updateBook(Integer id, BookFormDTO dto, String coverUrl);

    void deleteBook(Integer id);

    List<BookCategory> listCategories();

    void createCategory(BookCategoryFormDTO dto);

    void deleteCategory(Integer id);

    BigDecimal touchProgress(Integer bookId, Integer chapterId, String userUuid);

    void saveProgress(Integer bookId, Integer chapterId, BigDecimal scrollPercent, String userUuid);
}