package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.dto.request.BookChapterFormDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.dto.response.ChapterSummaryDTO;
import xyz.lingview.astra.dto.response.ParsedChapterDTO;
import xyz.lingview.astra.mapper.BookChapterMapper;
import xyz.lingview.astra.service.BookChapterService;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class BookChapterServiceImpl implements BookChapterService {

    private static final long MAX_IMPORT_SIZE = 20L * 1024 * 1024;
    private static final int MAX_IMPORT_CHAPTERS = 2000;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int SORT_STEP = 10;
    private static final String FALLBACK_TITLE = "导入章节";
    private static final Pattern CHAPTER_HEADING = Pattern.compile(
            "^\\s*(第\\s*[0-9一二三四五六七八九十百千零〇两]+\\s*[章回节卷篇]|(?i:chapter)\\s+\\d+).{0,40}$");

    @Autowired
    private BookChapterMapper chapterMapper;

    @Override
    public List<ChapterSummaryDTO> listAllChapters(Integer bookId) {
        return chapterMapper.selectAllChapterSummaries(bookId);
    }

    @Override
    public PageResult<ChapterSummaryDTO> listChapterPage(Integer bookId, int pageNum, int pageSize) {
        int page = Math.max(pageNum, 1);
        int size = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        int total = chapterMapper.countActiveChapters(bookId);
        List<ChapterSummaryDTO> data = chapterMapper.selectChapterSummaries(
                bookId, (page - 1) * size, size);

        PageResult<ChapterSummaryDTO> result = new PageResult<>();
        result.setData(data);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setTotal_pages((int) Math.ceil((double) total / size));
        return result;
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
        chapter.setWord_count(wordCount(dto.getContent()));
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
        chapter.setWord_count(wordCount(dto.getContent()));
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
        if (new LinkedHashSet<>(ids).size() != ids.size()) {
            throw new RuntimeException("章节列表已变化，请刷新后重试");
        }
        List<ChapterSummaryDTO> rows = chapterMapper.selectChapterSummariesByIds(bookId, ids);
        if (rows.size() != ids.size()) {
            throw new RuntimeException("章节列表已变化，请刷新后重试");
        }
        List<Integer> positions = new ArrayList<>();
        Map<Integer, Integer> indexOf = new HashMap<>();
        List<ChapterSummaryDTO> all = chapterMapper.selectAllChapterSummaries(bookId);
        for (int i = 0; i < all.size(); i++) {
            indexOf.put(all.get(i).getId(), i);
        }
        for (Integer id : ids) {
            Integer position = indexOf.get(id);
            if (position == null) {
                throw new RuntimeException("章节列表已变化，请刷新后重试");
            }
            positions.add(position);
        }
        Collections.sort(positions);
        for (int i = 1; i < positions.size(); i++) {
            if (positions.get(i) != positions.get(i - 1) + 1) {
                throw new RuntimeException("只能在当前页内调整章节顺序");
            }
        }
        List<Integer> orderPool = new ArrayList<>();
        for (ChapterSummaryDTO row : rows) {
            orderPool.add(row.getSort_order());
        }
        Collections.sort(orderPool);
        for (int i = 0; i < ids.size(); i++) {
            chapterMapper.updateChapterSort(ids.get(i), orderPool.get(i));
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
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败");
        }
        return importParsedChapters(bookId, parseChapters(file.getOriginalFilename(), bytes));
    }

    @Override
    public List<ParsedChapterDTO> parseChapters(String filename, byte[] bytes) {
        if (filename == null) {
            throw new RuntimeException("请选择要导入的文件");
        }
        if (bytes == null || bytes.length == 0) {
            throw new RuntimeException("文件内容为空");
        }
        if (bytes.length > MAX_IMPORT_SIZE) {
            throw new RuntimeException("文件过大，最大支持 20MB");
        }
        String lowerName = filename.toLowerCase();
        if (!lowerName.endsWith(".txt") && !lowerName.endsWith(".epub")) {
            throw new RuntimeException("仅支持 .txt 或 .epub 文件");
        }

        List<String> titles = new ArrayList<>();
        List<String> bodies = new ArrayList<>();
        if (lowerName.endsWith(".epub")) {
            parseEpub(bytes, titles, bodies);
        } else {
            splitChapters(decode(bytes), fallbackTitle(filename), titles, bodies);
        }
        if (titles.size() > MAX_IMPORT_CHAPTERS) {
            throw new RuntimeException("单次最多导入 " + MAX_IMPORT_CHAPTERS + " 章");
        }

        List<ParsedChapterDTO> chapters = new ArrayList<>(titles.size());
        for (int i = 0; i < titles.size(); i++) {
            String body = bodies.get(i).trim();
            if (body.isEmpty()) {
                continue;
            }
            chapters.add(new ParsedChapterDTO(titles.get(i), body));
        }
        if (chapters.isEmpty()) {
            throw new RuntimeException("未解析到有效章节内容");
        }
        return chapters;
    }

    @Override
    @Transactional
    public int importParsedChapters(Integer bookId, List<ParsedChapterDTO> chapters) {
        if (chapters == null || chapters.isEmpty()) {
            throw new RuntimeException("未解析到有效章节内容");
        }
        if (chapters.size() > MAX_IMPORT_CHAPTERS) {
            throw new RuntimeException("单次最多导入 " + MAX_IMPORT_CHAPTERS + " 章");
        }
        int sort = Optional.ofNullable(chapterMapper.selectMaxSortOrder(bookId)).orElse(0);
        int imported = 0;
        for (ParsedChapterDTO parsed : chapters) {
            String body = parsed.getContent() == null ? "" : parsed.getContent().trim();
            if (body.isEmpty()) {
                continue;
            }
            String title = parsed.getTitle() == null ? "" : parsed.getTitle().trim();
            if (title.isEmpty()) {
                title = FALLBACK_TITLE;
            }
            if (title.length() > 255) {
                title = title.substring(0, 255);
            }
            sort += SORT_STEP;
            BookChapter chapter = new BookChapter();
            chapter.setBook_id(bookId);
            chapter.setTitle(title);
            chapter.setContent(body);
            chapter.setWord_count(wordCount(body));
            chapter.setSort_order(sort);
            chapterMapper.insertChapter(chapter);
            imported++;
        }
        if (imported == 0) {
            throw new RuntimeException("未解析到有效章节内容");
        }
        return imported;
    }

    private void parseEpub(byte[] bytes, List<String> titles, List<String> bodies) {
        Map<String, byte[]> entries = readZipEntries(bytes);
        byte[] containerBytes = entries.get("META-INF/container.xml");
        if (containerBytes == null) {
            throw new RuntimeException("EPUB 缺少 container.xml，文件可能已损坏");
        }
        Document container = parseXml(containerBytes);
        NodeList rootfiles = elements(container, "rootfile");
        String opfPath = null;
        for (int i = 0; i < rootfiles.getLength(); i++) {
            String path = ((Element) rootfiles.item(i)).getAttribute("full-path");
            if (path != null && !path.isEmpty()) {
                opfPath = path;
                break;
            }
        }
        if (opfPath == null) {
            throw new RuntimeException("EPUB 缺少目录描述文件");
        }
        byte[] opfBytes = entries.get(opfPath);
        if (opfBytes == null) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(opfPath)) {
                    opfBytes = entry.getValue();
                    break;
                }
            }
        }
        if (opfBytes == null) {
            throw new RuntimeException("EPUB 目录描述文件缺失");
        }

        Document opf = parseXml(opfBytes);
        String opfDir = opfPath.contains("/") ? opfPath.substring(0, opfPath.lastIndexOf('/')) : "";
        Map<String, String> manifest = new HashMap<>();
        NodeList items = elements(opf, "item");
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            manifest.put(item.getAttribute("id"), item.getAttribute("href"));
        }

        List<String> spinePaths = new ArrayList<>();
        NodeList itemrefs = elements(opf, "itemref");
        for (int i = 0; i < itemrefs.getLength(); i++) {
            String idref = ((Element) itemrefs.item(i)).getAttribute("idref");
            String href = manifest.get(idref);
            if (href != null && !href.isEmpty()) {
                spinePaths.add(resolvePath(opfDir, href));
            }
        }
        if (spinePaths.isEmpty()) {
            for (String href : manifest.values()) {
                String lower = href.toLowerCase();
                if (lower.endsWith(".xhtml") || lower.endsWith(".html") || lower.endsWith(".htm")) {
                    spinePaths.add(resolvePath(opfDir, href));
                }
            }
        }

        int index = 0;
        for (String path : spinePaths) {
            byte[] data = entries.get(path);
            if (data == null) {
                continue;
            }
            String html = decode(data);
            String text = htmlToText(html);
            if (text.isEmpty()) {
                continue;
            }
            index++;
            String title = extractHtmlTitle(html);
            if (title == null || title.isEmpty()) {
                title = "第 " + index + " 节";
            }
            if (title.length() > 100) {
                title = title.substring(0, 100);
            }
            titles.add(title);
            bodies.add(text);
            if (titles.size() >= MAX_IMPORT_CHAPTERS) {
                break;
            }
        }
        if (titles.isEmpty()) {
            throw new RuntimeException("EPUB 未解析到有效章节内容");
        }
    }

    private Map<String, byte[]> readZipEntries(byte[] bytes) {
        Map<String, byte[]> entries = new HashMap<>();
        long total = 0;
        byte[] buffer = new byte[8192];
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                long entrySize = 0;
                int read;
                while ((read = zis.read(buffer)) != -1) {
                    entrySize += read;
                    total += read;
                    if (entrySize > MAX_IMPORT_SIZE || total > MAX_IMPORT_SIZE * 3L) {
                        throw new RuntimeException("EPUB 解压后内容过大");
                    }
                    out.write(buffer, 0, read);
                }
                entries.put(entry.getName().replace('\\', '/'), out.toByteArray());
                if (entries.size() > 5000) {
                    throw new RuntimeException("EPUB 文件条目过多");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("读取 EPUB 失败");
        }
        return entries;
    }

    private Document parseXml(byte[] bytes) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            log.warn("解析 EPUB XML 失败", e);
            throw new RuntimeException("解析 EPUB 文件失败");
        }
    }

    private NodeList elements(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() == 0) {
            nodes = document.getElementsByTagName(tagName);
        }
        return nodes;
    }

    private String resolvePath(String baseDir, String href) {
        String path = href;
        try {
            path = java.net.URLDecoder.decode(href, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
        int hash = path.indexOf('#');
        if (hash >= 0) {
            path = path.substring(0, hash);
        }
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        String combined = baseDir.isEmpty() ? path : baseDir + "/" + path;
        java.util.LinkedList<String> parts = new java.util.LinkedList<>();
        for (String part : combined.split("/")) {
            if (part.isEmpty() || ".".equals(part)) {
                continue;
            }
            if ("..".equals(part)) {
                if (!parts.isEmpty()) {
                    parts.removeLast();
                }
            } else {
                parts.add(part);
            }
        }
        return String.join("/", parts);
    }

    private String htmlToText(String html) {
        String cleaned = html
                .replaceAll("(?is)<head.*?</head>", " ")
                .replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</(p|div|li|h[1-6]|tr|section|article|blockquote)>", "\n")
                .replaceAll("(?s)<[^>]+>", " ");
        cleaned = decodeEntities(cleaned)
                .replace('\u00a0', ' ')
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n\\s*\\n+", "\n\n");
        return cleaned.trim();
    }

    private String extractHtmlTitle(String html) {
        Matcher titleMatcher = Pattern.compile("(?is)<title[^>]*>(.*?)</title>").matcher(html);
        if (titleMatcher.find()) {
            String title = decodeEntities(titleMatcher.group(1).replaceAll("<[^>]+>", " ")).trim();
            if (!title.isEmpty()) {
                return title;
            }
        }
        Matcher headingMatcher = Pattern.compile("(?is)<h[1-3][^>]*>(.*?)</h[1-3]>").matcher(html);
        if (headingMatcher.find()) {
            String title = decodeEntities(headingMatcher.group(1).replaceAll("<[^>]+>", " "))
                    .replaceAll("\\s+", " ").trim();
            if (!title.isEmpty()) {
                return title;
            }
        }
        return null;
    }

    private String decodeEntities(String text) {
        String result = text
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'");
        result = replaceNumericEntities(result, Pattern.compile("&#x([0-9a-fA-F]+);"), 16);
        return replaceNumericEntities(result, Pattern.compile("&#(\\d+);"), 10);
    }

    private String replaceNumericEntities(String text, Pattern pattern, int radix) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement;
            try {
                replacement = new String(Character.toChars(Integer.parseInt(matcher.group(1), radix)));
            } catch (IllegalArgumentException e) {
                replacement = "";
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
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