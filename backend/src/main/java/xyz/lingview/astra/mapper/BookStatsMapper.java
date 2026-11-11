package xyz.lingview.astra.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.lingview.astra.dto.response.BookStatsDTO;
import xyz.lingview.astra.dto.response.BookWordStatDTO;
import xyz.lingview.astra.dto.response.BucketStatDTO;
import xyz.lingview.astra.dto.response.CategoryStatDTO;
import xyz.lingview.astra.dto.response.DailyReadingDTO;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BookStatsMapper {

    BookStatsDTO selectTotals();

    List<CategoryStatDTO> selectCategoryStats();

    List<BookWordStatDTO> selectTopBooks(@Param("limit") int limit);

    List<BucketStatDTO> selectChapterBuckets();

    List<DailyReadingDTO> selectDailyReading(@Param("fromDate") String fromDate);

    Double selectAvgCompleteRate();

    int addReadingSeconds(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                          @Param("statDate") String statDate, @Param("seconds") int seconds,
                          @Param("now") LocalDateTime now);

    int insertReadingRow(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                         @Param("statDate") String statDate, @Param("seconds") int seconds,
                         @Param("now") LocalDateTime now);

    int insertChapterEvent(@Param("userUuid") String userUuid, @Param("bookId") Integer bookId,
                           @Param("chapterId") Integer chapterId, @Param("eventTime") LocalDateTime eventTime);
}