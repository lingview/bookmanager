package xyz.lingview.astra.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.dto.request.BookChapterFormDTO;
import xyz.lingview.astra.dto.response.ChapterSummaryDTO;

import java.util.List;

public interface BookChapterService {

    List<ChapterSummaryDTO> listChapters(Integer bookId);

    BookChapter getChapter(Integer bookId, Integer chapterId);

    void createChapter(Integer bookId, BookChapterFormDTO dto);

    void updateChapter(Integer bookId, Integer chapterId, BookChapterFormDTO dto);

    void deleteChapter(Integer bookId, Integer chapterId);

    void reorder(Integer bookId, List<Integer> ids);

    int importChapters(Integer bookId, MultipartFile file);
}