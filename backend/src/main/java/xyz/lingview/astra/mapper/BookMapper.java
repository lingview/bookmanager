package xyz.lingview.astra.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.lingview.astra.domain.Book;
import xyz.lingview.astra.domain.BookCategory;
import xyz.lingview.astra.domain.BookReadingProgress;
import xyz.lingview.astra.dto.response.BookViewDTO;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BookMapper {

    int countBooks(@Param("keywordPattern") String keywordPattern, @Param("categoryId") Integer categoryId);

    List<BookViewDTO> selectBooks(@Param("keywordPattern") String keywordPattern,
                                  @Param("categoryId") Integer categoryId,
                                  @Param("userUuid") String userUuid,
                                  @Param("offset") int offset, @Param("limit") int limit);

    BookViewDTO selectBook(@Param("id") Integer id, @Param("userUuid") String userUuid);

    int insertBook(Book book);

    int updateBook(Book book);

    int softDeleteBook(@Param("id") Integer id);

    List<BookCategory> selectCategories();

    int countCategoryByName(@Param("name") String name);

    int countBooksByCategory(@Param("categoryId") Integer categoryId);

    int insertCategory(BookCategory category);

    int deleteCategory(@Param("id") Integer id);

    BookReadingProgress selectProgress(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId);

    int insertProgress(BookReadingProgress progress);

    int updateProgressChapter(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                              @Param("chapterId") Integer chapterId);

    int updateProgressScroll(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                             @Param("chapterId") Integer chapterId, @Param("scrollPercent") BigDecimal scrollPercent);
}