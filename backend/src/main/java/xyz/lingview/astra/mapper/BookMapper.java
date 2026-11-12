package xyz.lingview.astra.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.lingview.astra.domain.Book;
import xyz.lingview.astra.domain.BookBookmark;
import xyz.lingview.astra.domain.BookCategory;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.domain.BookReadingProgress;
import xyz.lingview.astra.dto.response.BookViewDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BookMapper {

    int countBooks(@Param("keywordPattern") String keywordPattern,
                   @Param("categoryId") Integer categoryId,
                   @Param("shelfStatus") String shelfStatus,
                   @Param("tagName") String tagName,
                   @Param("userUuid") String userUuid);

    List<BookViewDTO> selectBooks(@Param("keywordPattern") String keywordPattern,
                                  @Param("categoryId") Integer categoryId,
                                  @Param("shelfStatus") String shelfStatus,
                                  @Param("tagName") String tagName,
                                  @Param("sort") String sort,
                                  @Param("userUuid") String userUuid,
                                  @Param("offset") int offset, @Param("limit") int limit);

    Integer selectBookIdAtOffset(@Param("offset") int offset);

    List<BookViewDTO> selectRecentReading(@Param("userUuid") String userUuid, @Param("limit") int limit);

    BookViewDTO selectBook(@Param("id") Integer id, @Param("userUuid") String userUuid);

    int insertBook(Book book);

    int updateBook(Book book);

    int softDeleteBook(@Param("id") Integer id);

    List<BookCategory> selectCategories();

    int countCategoryByName(@Param("name") String name);

    int countCategoryByNameExcluding(@Param("name") String name, @Param("id") Integer id);

    int countBooksByCategory(@Param("categoryId") Integer categoryId);

    int insertCategory(BookCategory category);

    int updateCategory(@Param("id") Integer id, @Param("name") String name, @Param("sortOrder") Integer sortOrder);

    int deleteCategory(@Param("id") Integer id);

    String selectShelfStatus(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId);

    int insertShelf(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                    @Param("status") String status, @Param("now") java.time.LocalDateTime now);

    int updateShelfStatus(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                          @Param("status") String status, @Param("now") java.time.LocalDateTime now);

    int deleteShelf(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId);

    int countReview(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId);

    int insertReview(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                     @Param("rating") Integer rating, @Param("review") String review,
                     @Param("now") java.time.LocalDateTime now);

    int updateReview(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                     @Param("rating") Integer rating, @Param("review") String review,
                     @Param("now") java.time.LocalDateTime now);

    int deleteReview(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId);

    BookReadingProgress selectProgress(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId);

    int insertProgress(BookReadingProgress progress);

    int updateProgressChapter(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                              @Param("chapterId") Integer chapterId);

    int updateProgressScroll(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                             @Param("chapterId") Integer chapterId, @Param("scrollPercent") BigDecimal scrollPercent);

    List<BookBookmark> selectBookmarks(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId);

    int insertBookmark(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                       @Param("chapterId") Integer chapterId, @Param("scrollPercent") BigDecimal scrollPercent,
                       @Param("note") String note, @Param("now") LocalDateTime now);

    int deleteBookmark(@Param("id") Integer id, @Param("userUuid") String userUuid);

    Integer selectLatestBookmarkId(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                                   @Param("chapterId") Integer chapterId);

    Integer selectChapterBookId(@Param("chapterId") Integer chapterId);

    List<BookChapter> searchChapters(@Param("bookId") Integer bookId, @Param("pattern") String pattern);

    List<xyz.lingview.astra.domain.BookTag> selectAllTags();

    Integer selectTagIdByName(@Param("name") String name);

    int insertTag(xyz.lingview.astra.domain.BookTag tag);

    int deleteBookTags(@Param("bookId") Integer bookId);

    int insertBookTag(@Param("bookId") Integer bookId, @Param("tagId") Integer tagId);

    List<xyz.lingview.astra.dto.response.FullTextRowDTO> searchAllChapters(@Param("pattern") String pattern);

    List<xyz.lingview.astra.domain.BookNote> selectNotes(@Param("userUuid") String userUuid,
                                                         @Param("bookId") Integer bookId);

    int insertNote(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                   @Param("chapterId") Integer chapterId, @Param("quoteText") String quoteText,
                   @Param("noteText") String noteText, @Param("now") LocalDateTime now);

    int deleteNote(@Param("id") Integer id, @Param("userUuid") String userUuid);
}