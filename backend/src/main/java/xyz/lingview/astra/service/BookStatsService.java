package xyz.lingview.astra.service;

import xyz.lingview.astra.dto.response.BookStatsDTO;
import xyz.lingview.astra.dto.response.HomeOverviewDTO;
import xyz.lingview.astra.dto.response.PersonalStatsDTO;

public interface BookStatsService {

    void recordHeartbeat(Integer bookId, int seconds, String userUuid);

    void recordChapterOpen(Integer bookId, Integer chapterId, String userUuid);

    BookStatsDTO getStats();

    HomeOverviewDTO getPersonalOverview(String userUuid);

    PersonalStatsDTO getPersonalStats(String userUuid, int year);

    void saveGoal(String userUuid, int year, int target);
}