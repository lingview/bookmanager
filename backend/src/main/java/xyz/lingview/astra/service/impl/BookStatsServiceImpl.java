package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.dto.response.BookStatsDTO;
import xyz.lingview.astra.dto.response.DailyReadingDTO;
import xyz.lingview.astra.dto.response.FinishedBookDTO;
import xyz.lingview.astra.dto.response.HomeOverviewDTO;
import xyz.lingview.astra.dto.response.PersonalStatsDTO;
import xyz.lingview.astra.mapper.BookStatsMapper;
import xyz.lingview.astra.service.BookStatsService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class BookStatsServiceImpl implements BookStatsService {

    private static final int MAX_HEARTBEAT_SECONDS = 120;
    private static final int TREND_DAYS = 14;
    private static final int STREAK_SCAN_DAYS = 366;

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

    @Override
    public HomeOverviewDTO getPersonalOverview(String userUuid) {
        HomeOverviewDTO overview = new HomeOverviewDTO();
        if (userUuid == null) {
            overview.setWeek_text(formatDuration(0));
            overview.setTotal_text(formatDuration(0));
            return overview;
        }

        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Long weekSeconds = bookStatsMapper.sumPersonalReadingSeconds(userUuid, monday.toString());
        Long totalSeconds = bookStatsMapper.sumPersonalReadingSeconds(userUuid, null);
        overview.setWeek_seconds(weekSeconds == null ? 0 : weekSeconds);
        overview.setTotal_seconds(totalSeconds == null ? 0 : totalSeconds);
        overview.setReading_book_count(bookStatsMapper.countPersonalReadingBooks(userUuid));
        overview.setStreak_days(calcStreak(userUuid));

        String yearPrefix = LocalDate.now().getYear() + "-";
        int finishedThisYear = 0;
        for (FinishedBookDTO item : bookStatsMapper.selectFinishedBooks(userUuid)) {
            if (item.getFinish_time() != null && item.getFinish_time().startsWith(yearPrefix)) {
                finishedThisYear++;
            }
        }
        overview.setFinished_this_year(finishedThisYear);
        Integer goal = bookStatsMapper.selectGoalTarget(userUuid, LocalDate.now().getYear());
        overview.setYear_goal(goal == null ? 0 : goal);

        overview.setWeek_text(formatDuration(overview.getWeek_seconds()));
        overview.setTotal_text(formatDuration(overview.getTotal_seconds()));
        return overview;
    }

    @Override
    public PersonalStatsDTO getPersonalStats(String userUuid, int year) {
        PersonalStatsDTO stats = new PersonalStatsDTO();
        stats.setYear(year);
        if (userUuid == null) {
            stats.setFinished_books(List.of());
            stats.setDaily(List.of());
            stats.setCategories(List.of());
            stats.setTotal_text(formatDuration(0));
            stats.setWeek_text(formatDuration(0));
            return stats;
        }

        LocalDate today = LocalDate.now();
        stats.setDaily(bookStatsMapper.selectPersonalDaily(userUuid, today.minusDays(364).toString()));
        stats.setDays_count(bookStatsMapper.selectPersonalReadingDates(userUuid, "1970-01-01").size());
        stats.setStreak_days(calcStreak(userUuid));

        Long total = bookStatsMapper.sumPersonalReadingSeconds(userUuid, null);
        stats.setTotal_seconds(total == null ? 0 : total);
        stats.setTotal_text(formatDuration(stats.getTotal_seconds()));

        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Long week = bookStatsMapper.sumPersonalReadingSeconds(userUuid, monday.toString());
        stats.setWeek_seconds(week == null ? 0 : week);
        stats.setWeek_text(formatDuration(stats.getWeek_seconds()));

        List<FinishedBookDTO> finished = bookStatsMapper.selectFinishedBooks(userUuid);
        stats.setFinished_books(finished);
        stats.setFinished_count(finished.size());
        String yearPrefix = year + "-";
        int yearFinished = 0;
        for (FinishedBookDTO item : finished) {
            if (item.getFinish_time() != null && item.getFinish_time().startsWith(yearPrefix)) {
                yearFinished++;
            }
        }
        stats.setYear_finished(yearFinished);
        Integer goal = bookStatsMapper.selectGoalTarget(userUuid, year);
        stats.setYear_goal(goal == null ? 0 : goal);
        stats.setCategories(bookStatsMapper.selectPersonalCategorySeconds(userUuid));
        return stats;
    }

    @Override
    public void saveGoal(String userUuid, int year, int target) {
        if (userUuid == null) {
            throw new RuntimeException("用户未登录");
        }
        if (year < 2000 || year > 2100) {
            throw new RuntimeException("目标年份不合法");
        }
        if (target < 0 || target > 9999) {
            throw new RuntimeException("目标本数需在 0~9999 之间");
        }
        LocalDateTime now = LocalDateTime.now();
        Integer existing = bookStatsMapper.selectGoalTarget(userUuid, year);
        if (target == 0) {
            if (existing != null) {
                bookStatsMapper.deleteGoal(userUuid, year);
            }
        } else if (existing == null) {
            bookStatsMapper.insertGoal(userUuid, year, target, now);
        } else {
            bookStatsMapper.updateGoal(userUuid, year, target, now);
        }
    }

    private int calcStreak(String userUuid) {
        LocalDate today = LocalDate.now();
        Set<String> dates = new HashSet<>(bookStatsMapper.selectPersonalReadingDates(
                userUuid, today.minusDays(STREAK_SCAN_DAYS).toString()));
        LocalDate cursor = today;
        if (!dates.contains(cursor.toString())) {
            cursor = today.minusDays(1);
        }
        int streak = 0;
        while (dates.contains(cursor.toString())) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private String formatDuration(long seconds) {
        if (seconds <= 0) {
            return "0 分钟";
        }
        if (seconds < 3600) {
            return Math.max(1, seconds / 60) + " 分钟";
        }
        double hours = seconds / 3600.0;
        String text = hours >= 10
                ? String.valueOf(Math.round(hours))
                : String.format(Locale.ROOT, "%.1f", hours);
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text + " 小时";
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