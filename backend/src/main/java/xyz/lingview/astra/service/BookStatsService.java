package xyz.lingview.astra.service;

import xyz.lingview.astra.dto.response.BookStatsDTO;

public interface BookStatsService {

    void recordHeartbeat(Integer bookId, int seconds, String userUuid);

    void recordChapterOpen(Integer bookId, Integer chapterId, String userUuid);

    BookStatsDTO getStats();
}