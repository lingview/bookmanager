package xyz.lingview.astra.service.impl;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.lingview.astra.dto.response.ImportPreviewDTO;
import xyz.lingview.astra.dto.response.ParsedChapterDTO;
import xyz.lingview.astra.dto.response.PreviewChapterContentDTO;
import xyz.lingview.astra.dto.response.PreviewChapterSummaryDTO;
import xyz.lingview.astra.service.BookChapterService;
import xyz.lingview.astra.service.ImportPreviewService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImportPreviewServiceImpl implements ImportPreviewService {

    private static final String SESSION_KEY = "bookImportPreviews";
    private static final long MAX_AGE_MS = 2 * 60 * 60 * 1000L;
    private static final int MAX_TITLE_LENGTH = 255;

    @Autowired
    private BookChapterService bookChapterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ImportPreviewDTO createPreview(MultipartFile file, HttpSession session) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要解析的文件");
        }
        String filename = file.getOriginalFilename();
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
        List<ParsedChapterDTO> chapters = bookChapterService.parseChapters(filename, bytes);

        String token = UUID.randomUUID().toString().replace("-", "");
        File target = new File(baseDir(), token + ".json");
        writePreviewFile(target, filename, chapters);

        Map<String, StagedImport> staged = stagedMap(session);
        pruneExpired(staged);
        StagedImport item = new StagedImport();
        item.setPath(target.getPath());
        item.setFilename(filename);
        item.setCreatedAt(System.currentTimeMillis());
        staged.put(token, item);
        session.setAttribute(SESSION_KEY, staged);

        return toSummary(token, filename, chapters);
    }

    @Override
    public ImportPreviewDTO getPreview(String token, HttpSession session) {
        PreviewFile preview = readPreview(token, session);
        return toSummary(token, preview.getFilename(), preview.getChapters());
    }

    @Override
    public PreviewChapterContentDTO getChapter(String token, HttpSession session, int index) {
        PreviewFile preview = readPreview(token, session);
        List<ParsedChapterDTO> chapters = preview.getChapters();
        if (index < 0 || index >= chapters.size()) {
            throw new RuntimeException("章节不存在");
        }
        ParsedChapterDTO chapter = chapters.get(index);
        PreviewChapterContentDTO dto = new PreviewChapterContentDTO();
        dto.setIndex(index);
        dto.setTitle(chapter.getTitle());
        dto.setContent(chapter.getContent());
        dto.setWord_count(wordCount(chapter.getContent()));
        return dto;
    }

    @Override
    public ImportPreviewDTO saveChapter(String token, HttpSession session, int index, String title, String content) {
        StagedImport staged = requireStaged(token, session);
        PreviewFile preview = readPreviewFile(staged);
        List<ParsedChapterDTO> chapters = preview.getChapters();
        if (index < 0 || index >= chapters.size()) {
            throw new RuntimeException("章节不存在");
        }
        String normalizedTitle = title == null ? "" : title.trim();
        if (normalizedTitle.isEmpty()) {
            throw new RuntimeException("章节标题不能为空");
        }
        if (normalizedTitle.length() > MAX_TITLE_LENGTH) {
            normalizedTitle = normalizedTitle.substring(0, MAX_TITLE_LENGTH);
        }
        String normalizedContent = content == null ? "" : content;
        if (normalizedContent.trim().isEmpty()) {
            throw new RuntimeException("章节内容不能为空");
        }
        chapters.set(index, new ParsedChapterDTO(normalizedTitle, normalizedContent));
        writePreviewFile(new File(staged.getPath()), preview.getFilename(), chapters);
        return toSummary(token, preview.getFilename(), chapters);
    }

    @Override
    public ImportPreviewDTO deleteChapter(String token, HttpSession session, int index) {
        StagedImport staged = requireStaged(token, session);
        PreviewFile preview = readPreviewFile(staged);
        List<ParsedChapterDTO> chapters = preview.getChapters();
        if (index < 0 || index >= chapters.size()) {
            throw new RuntimeException("章节不存在");
        }
        if (chapters.size() <= 1) {
            throw new RuntimeException("至少保留一章");
        }
        chapters.remove(index);
        writePreviewFile(new File(staged.getPath()), preview.getFilename(), chapters);
        return toSummary(token, preview.getFilename(), chapters);
    }

    @Override
    public void discard(String token, HttpSession session) {
        if (token == null || token.isEmpty()) {
            return;
        }
        Map<String, StagedImport> staged = stagedMap(session);
        StagedImport item = staged.remove(token);
        if (item != null) {
            new File(item.getPath()).delete();
        }
        session.setAttribute(SESSION_KEY, staged);
    }

    @Override
    public List<ParsedChapterDTO> loadChapters(String token, HttpSession session) {
        PreviewFile preview = readPreview(token, session);
        if (preview.getChapters() == null || preview.getChapters().isEmpty()) {
            throw new RuntimeException("未解析到有效章节内容");
        }
        return new ArrayList<>(preview.getChapters());
    }

    private PreviewFile readPreview(String token, HttpSession session) {
        StagedImport staged = requireStaged(token, session);
        return readPreviewFile(staged);
    }

    private StagedImport requireStaged(String token, HttpSession session) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("预览已失效，请重新上传文件");
        }
        Map<String, StagedImport> staged = stagedMap(session);
        StagedImport item = staged.get(token);
        if (item == null) {
            throw new RuntimeException("预览已失效，请重新上传文件");
        }
        if (System.currentTimeMillis() - item.getCreatedAt() > MAX_AGE_MS) {
            staged.remove(token);
            new File(item.getPath()).delete();
            session.setAttribute(SESSION_KEY, staged);
            throw new RuntimeException("预览已过期，请重新上传文件");
        }
        return item;
    }

    private PreviewFile readPreviewFile(StagedImport staged) {
        File file = new File(staged.getPath());
        if (!file.exists()) {
            throw new RuntimeException("预览文件已失效，请重新上传");
        }
        try {
            return objectMapper.readValue(file, PreviewFile.class);
        } catch (Exception e) {
            throw new RuntimeException("读取预览数据失败");
        }
    }

    private void writePreviewFile(File target, String filename, List<ParsedChapterDTO> chapters) {
        PreviewFile data = new PreviewFile();
        data.setFilename(filename);
        data.setChapters(chapters);
        try {
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            objectMapper.writeValue(target, data);
        } catch (Exception e) {
            throw new RuntimeException("保存预览数据失败");
        }
    }

    private ImportPreviewDTO toSummary(String token, String filename, List<ParsedChapterDTO> chapters) {
        ImportPreviewDTO dto = new ImportPreviewDTO();
        dto.setToken(token);
        dto.setFilename(filename);
        dto.setChapter_count(chapters.size());
        List<PreviewChapterSummaryDTO> summaries = new ArrayList<>(chapters.size());
        int totalWords = 0;
        for (int i = 0; i < chapters.size(); i++) {
            ParsedChapterDTO chapter = chapters.get(i);
            PreviewChapterSummaryDTO summary = new PreviewChapterSummaryDTO();
            summary.setIndex(i);
            summary.setTitle(chapter.getTitle());
            int words = wordCount(chapter.getContent());
            summary.setWord_count(words);
            totalWords += words;
            summaries.add(summary);
        }
        dto.setTotal_words(totalWords);
        dto.setChapters(summaries);
        return dto;
    }

    @SuppressWarnings("unchecked")
    private Map<String, StagedImport> stagedMap(HttpSession session) {
        Object value = session.getAttribute(SESSION_KEY);
        if (value instanceof Map) {
            return (Map<String, StagedImport>) value;
        }
        Map<String, StagedImport> map = new HashMap<>();
        session.setAttribute(SESSION_KEY, map);
        return map;
    }

    private void pruneExpired(Map<String, StagedImport> staged) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, StagedImport>> iterator = staged.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, StagedImport> entry = iterator.next();
            if (now - entry.getValue().getCreatedAt() > MAX_AGE_MS) {
                new File(entry.getValue().getPath()).delete();
                iterator.remove();
            }
        }
        File[] files = baseDir().listFiles();
        if (files != null) {
            for (File file : files) {
                if (now - file.lastModified() > MAX_AGE_MS) {
                    file.delete();
                }
            }
        }
    }

    private File baseDir() {
        File dir = new File(System.getProperty("user.dir"), "temp/import");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("无法创建临时目录");
        }
        return dir;
    }

    private int wordCount(String content) {
        if (content == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < content.length(); i++) {
            if (!Character.isWhitespace(content.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    public static class StagedImport implements Serializable {
        private static final long serialVersionUID = 1L;
        private String path;
        private String filename;
        private long createdAt;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class PreviewFile {
        private String filename;
        private List<ParsedChapterDTO> chapters;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public List<ParsedChapterDTO> getChapters() {
            return chapters;
        }

        public void setChapters(List<ParsedChapterDTO> chapters) {
            this.chapters = chapters;
        }
    }
}
