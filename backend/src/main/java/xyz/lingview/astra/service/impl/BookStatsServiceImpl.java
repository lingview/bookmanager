package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.dto.response.BookStatsDTO;
import xyz.lingview.astra.dto.response.DailyReadingDTO;
import xyz.lingview.astra.mapper.BookStatsMapper;
import xyz.lingview.astra.service.BookStatsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BookStatsServiceImpl implements BookStatsService {

    private static final int MAX_HEARTBEAT_SECONDS = 120;
    private static final int TREND_DAYS = 14;

    @Autowired
    private BookStatsMapper bookStatsMapper;

    @Override
    public void recordHeartbeat(Integer bookId, int seconds, String userUuid) {
        if (bookId == null || userUuid == null || seconds <= 0) {
            return;
        }
        int value = Math.min(seconds, MAX_HEARTBEAT_SECONDS);
        String statDate = LocalDate.now().toString();
        LocalDateTime now = LocalDateTime.now();

        int updated = bookStatsMapper.addReadingSeconds(userUuid, bookId, statDate, value, now);
        if (updated == 0) {
            try {
                bookStatsMapper.insertReadingRow(userUuid, bookId, statDate, value, now);
            } catch (DataIntegrityViolationException e) {
                bookStatsMapper.addReadingSeconds(userUuid, bookId, statDate, value, now);
            }
        }
    }

    @Override
    public void recordChapterOpen(Integer bookId, Integer chapterId, String userUuid) {
        if (bookId == null || chapterId == null || userUuid == null) {
            return;
        }
        bookStatsMapper.insertChapterEvent(userUuid, bookId, chapterId, LocalDateTime.now());
    }

    @Override
    public BookStatsDTO getStats() {
        BookStatsDTO stats = bookStatsMapper.selectTotals();
        if (stats == null) {
            stats = new BookStatsDTO();
        }
        stats.setCategories(bookStatsMapper.selectCategoryStats());
        stats.setTop_books(bookStatsMapper.selectTopBooks(10));
        stats.setChapter_buckets(bookStatsMapper.selectChapterBuckets());
        stats.setDaily_reading(buildDailyTrend());

        Double completeRate = bookStatsMapper.selectAvgCompleteRate();
        stats.setComplete_rate(completeRate == null ? 0 : Math.round(completeRate * 1000) / 10.0);
        return stats;
    }

    private List<DailyReadingDTO> buildDailyTrend() {
        LocalDate from = LocalDate.now().minusDays(TREND_DAYS - 1);
        Map<String, Long> byDate = new HashMap<>();
        for (DailyReadingDTO item : bookStatsMapper.selectDailyReading(from.toString())) {
            byDate.put(item.getStat_date(), item.getSeconds());
        }

        List<DailyReadingDTO> trend = new ArrayList<>(TREND_DAYS);
        for (int i = 0; i < TREND_DAYS; i++) {
            String date = from.plusDays(i).toString();
            DailyReadingDTO item = new DailyReadingDTO();
            item.setStat_date(date);
            item.setSeconds(byDate.getOrDefault(date, 0L));
            trend.add(item);
        }
        return trend;
    }
}