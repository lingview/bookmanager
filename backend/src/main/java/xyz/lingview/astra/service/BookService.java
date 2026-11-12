package xyz.lingview.astra.service;

import xyz.lingview.astra.domain.BookBookmark;
import xyz.lingview.astra.domain.BookCategory;
import xyz.lingview.astra.domain.BookNote;
import xyz.lingview.astra.domain.BookTag;
import xyz.lingview.astra.dto.request.BookCategoryFormDTO;
import xyz.lingview.astra.dto.request.BookFormDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.dto.response.BookViewDTO;
import xyz.lingview.astra.dto.response.ChapterSearchHitDTO;
import xyz.lingview.astra.dto.response.FullTextHitDTO;
import xyz.lingview.astra.dto.response.ParsedChapterDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {

    PageResult<BookViewDTO> listBooks(String keyword, Integer categoryId, String shelfStatus, String tagName,
                                      String sort, int pageNum, int pageSize, String userUuid);

    List<BookTag> listTags();

    List<BookViewDTO> listRecentReading(String userUuid, int limit);

    Integer randomBookId();

    void saveMyReading(Integer bookId, String status, Integer rating, String review, String userUuid);

    BookViewDTO getBook(Integer id, String userUuid);

    Integer createBook(BookFormDTO dto, String coverUrl);

    Integer createBookWithChapters(BookFormDTO dto, String coverUrl, List<ParsedChapterDTO> chapters);

    void updateBook(Integer id, BookFormDTO dto, String coverUrl);

    void deleteBook(Integer id);

    List<BookCategory> listCategories();

    void createCategory(BookCategoryFormDTO dto);

    void renameCategory(Integer id, String name, Integer sortOrder);

    void deleteCategory(Integer id);

    BigDecimal touchProgress(Integer bookId, Integer chapterId, String userUuid);

    void saveProgress(Integer bookId, Integer chapterId, BigDecimal scrollPercent, String userUuid);

    List<BookBookmark> listBookmarks(String userUuid, Integer bookId);

    void addBookmark(Integer bookId, Integer chapterId, BigDecimal scrollPercent, String note, String userUuid);

    void deleteBookmark(Integer id, String userUuid);

    List<ChapterSearchHitDTO> searchInBook(Integer bookId, String keyword);

    List<FullTextHitDTO> searchAll(String keyword);

    List<BookNote> listNotes(String userUuid, Integer bookId);

    void addNote(Integer bookId, Integer chapterId, String quoteText, String noteText, String userUuid);

    void deleteNote(Integer id, String userUuid);
}