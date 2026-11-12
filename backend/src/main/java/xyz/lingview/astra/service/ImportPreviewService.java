package xyz.lingview.astra.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;
import xyz.lingview.astra.dto.response.ImportPreviewDTO;
import xyz.lingview.astra.dto.response.ParsedChapterDTO;
import xyz.lingview.astra.dto.response.PreviewChapterContentDTO;

import java.util.List;

public interface ImportPreviewService {

    ImportPreviewDTO createPreview(MultipartFile file, HttpSession session);

    ImportPreviewDTO getPreview(String token, HttpSession session);

    PreviewChapterContentDTO getChapter(String token, HttpSession session, int index);

    ImportPreviewDTO saveChapter(String token, HttpSession session, int index, String title, String content);

    ImportPreviewDTO deleteChapter(String token, HttpSession session, int index);

    void discard(String token, HttpSession session);

    List<ParsedChapterDTO> loadChapters(String token, HttpSession session);
}
