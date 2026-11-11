package xyz.lingview.astra.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.dto.response.ChapterSummaryDTO;

import java.util.List;

@Mapper
public interface BookChapterMapper {

    List<ChapterSummaryDTO> selectAllChapterSummaries(@Param("bookId") Integer bookId);

    List<ChapterSummaryDTO> selectChapterSummaries(@Param("bookId") Integer bookId,
                                                   @Param("offset") int offset,
                                                   @Param("limit") int limit);

    List<ChapterSummaryDTO> selectChapterSummariesByIds(@Param("bookId") Integer bookId,
                                                       @Param("ids") List<Integer> ids);

    BookChapter selectChapter(@Param("id") Integer id);

    Integer selectMaxSortOrder(@Param("bookId") Integer bookId);

    int insertChapter(BookChapter chapter);

    int updateChapter(BookChapter chapter);

    int softDeleteChapter(@Param("id") Integer id);

    int updateChapterSort(@Param("id") Integer id, @Param("sort_order") Integer sort_order);

    int countActiveChapters(@Param("bookId") Integer bookId);
}