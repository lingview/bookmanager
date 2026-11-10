package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.dto.request.BookChapterFormDTO;
import xyz.lingview.astra.dto.response.ChapterSummaryDTO;
import xyz.lingview.astra.mapper.BookChapterMapper;
import xyz.lingview.astra.service.BookChapterService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class BookChapterServiceImpl implements BookChapterService {

    private static final long MAX_IMPORT_SIZE = 20L * 1024 * 1024;
    private static final int MAX_IMPORT_CHAPTERS = 2000;
    private static final int SORT_STEP = 10;
    private static final String FALLBACK_TITLE = "导入章节";
    private static final Pattern CHAPTER_HEADING = Pattern.compile(
            "^\\s*(第\\s*[0-9一二三四五六七八九十百千零〇两]+\\s*[章回节卷篇]|(?i:chapter)\\s+\\d+).{0,40}$");

    @Autowired
    private BookChapterMapper chapterMapper;

    @Override
    public List<ChapterSummaryDTO> listChapters(Integer bookId) {
        return chapterMapper.selectChapterSummaries(bookId);
    }

    @Override
    public BookChapter getChapter(Integer bookId, Integer chapterId) {
        if (chapterId == null) {
            return null;
        }
        BookChapter chapter = chapterMapper.selectChapter(chapterId);
        if (chapter == null || !bookId.equals(chapter.getBook_id())) {
            return null;
        }
        return chapter;
    }

    @Override
    public void createChapter(Integer bookId, BookChapterFormDTO dto) {
        BookChapter chapter = new BookChapter();
        chapter.setBook_id(bookId);
        chapter.setTitle(dto.getTitle().trim());
        chapter.setContent(dto.getContent());
        chapter.setSort_order(nextSortOrder(bookId));
        chapterMapper.insertChapter(chapter);
    }

    @Override
    public void updateChapter(Integer bookId, Integer chapterId, BookChapterFormDTO dto) {
        requireChapter(bookId, chapterId);
        BookChapter chapter = new BookChapter();
        chapter.setId(chapterId);
        chapter.setTitle(dto.getTitle().trim());
        chapter.setContent(dto.getContent());
        chapterMapper.updateChapter(chapter);
    }

    @Override
    public void deleteChapter(Integer bookId, Integer chapterId) {
        requireChapter(bookId, chapterId);
        chapterMapper.softDeleteChapter(chapterId);
    }

    @Override
    @Transactional
    public void reorder(Integer bookId, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("章节列表为空");
        }
        Set<Integer> existing = new LinkedHashSet<>();
        for (ChapterSummaryDTO chapter : chapterMapper.selectChapterSummaries(bookId)) {
            existing.add(chapter.getId());
        }
        Set<Integer> submitted = new LinkedHashSet<>(ids);
        if (submitted.size() != ids.size() || !existing.equals(submitted)) {
            throw new RuntimeException("章节列表已变化，请刷新后重试");
        }
        int order = SORT_STEP;
        for (Integer id : ids) {
            chapterMapper.updateChapterSort(id, order);
            order += SORT_STEP;
        }
    }

    @Override
    @Transactional
    public int importChapters(Integer bookId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要导入的文件");
        }
        if (file.getSize() > MAX_IMPORT_SIZE) {
            throw new RuntimeException("文件过大，最大支持 20MB");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".txt")) {
            throw new RuntimeException("仅支持 .txt 文本文件");
        }

        String text;
        try {
            text = decode(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }

        List<String> titles = new ArrayList<>();
        List<String> bodies = new ArrayList<>();
        splitChapters(text, fallbackTitle(filename), titles, bodies);
        if (titles.size() > MAX_IMPORT_CHAPTERS) {
            throw new RuntimeException("单次最多导入 " + MAX_IMPORT_CHAPTERS + " 章");
        }

        int sort = Optional.ofNullable(chapterMapper.selectMaxSortOrder(bookId)).orElse(0);
        int imported = 0;
        for (int i = 0; i < titles.size(); i++) {
            String body = bodies.get(i).trim();
            if (body.isEmpty()) {
                continue;
            }
            sort += SORT_STEP;
            BookChapter chapter = new BookChapter();
            chapter.setBook_id(bookId);
            chapter.setTitle(titles.get(i));
            chapter.setContent(body);
            chapter.setSort_order(sort);
            chapterMapper.insertChapter(chapter);
            imported++;
        }
        if (imported == 0) {
            throw new RuntimeException("未解析到有效章节内容");
        }
        return imported;
    }

    private void splitChapters(String text, String fallbackTitle, List<String> titles, List<String> bodies) {
        String currentTitle = null;
        StringBuilder currentBody = null;
        StringBuilder preamble = new StringBuilder();
        boolean foundHeading = false;

        for (String line : text.split("\r?\n", -1)) {
            String trimmed = line.trim();
            if (isHeading(trimmed)) {
                foundHeading = true;
                if (currentTitle != null) {
                    titles.add(currentTitle);
                    bodies.add(currentBody.toString());
                }
                currentTitle = trimmed;
                currentBody = new StringBuilder();
            } else if (currentTitle == null) {
                preamble.append(line).append('\n');
            } else {
                currentBody.append(line).append('\n');
            }
        }
        if (currentTitle != null) {
            titles.add(currentTitle);
            bodies.add(currentBody.toString());
        }
        if (!foundHeading) {
            titles.add(fallbackTitle);
            bodies.add(preamble.toString());
        } else if (!preamble.toString().trim().isEmpty() && !titles.isEmpty()) {
            bodies.set(0, preamble + "\n" + bodies.get(0));
        }
    }

    private boolean isHeading(String trimmed) {
        if (trimmed.isEmpty() || trimmed.length() > 60) {
            return false;
        }
        if (trimmed.endsWith("。") || trimmed.endsWith("！") || trimmed.endsWith("？") || trimmed.endsWith("，")) {
            return false;
        }
        return CHAPTER_HEADING.matcher(trimmed).matches();
    }

    private String decode(byte[] bytes) {
        int offset = 0;
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            offset = 3;
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset))
                    .toString();
        } catch (CharacterCodingException e) {
            try {
                return Charset.forName("GBK").newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset))
                        .toString();
            } catch (CharacterCodingException ex) {
                throw new RuntimeException("文件编码无法识别，请使用 UTF-8 或 GBK");
            }
        }
    }

    private String fallbackTitle(String filename) {
        String name = filename.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? FALLBACK_TITLE : trimmed;
    }

    private int nextSortOrder(Integer bookId) {
        Integer max = chapterMapper.selectMaxSortOrder(bookId);
        return (max == null ? 0 : max) + SORT_STEP;
    }

    private BookChapter requireChapter(Integer bookId, Integer chapterId) {
        BookChapter chapter = chapterId == null ? null : chapterMapper.selectChapter(chapterId);
        if (chapter == null || !bookId.equals(chapter.getBook_id())) {
            throw new RuntimeException("章节不存在或不属于该图书");
        }
        return chapter;
    }
}