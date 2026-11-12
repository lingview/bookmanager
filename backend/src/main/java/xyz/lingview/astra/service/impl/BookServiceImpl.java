package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.lingview.astra.domain.Book;
import xyz.lingview.astra.domain.BookBookmark;
import xyz.lingview.astra.domain.BookCategory;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.domain.BookNote;
import xyz.lingview.astra.domain.BookReadingProgress;
import xyz.lingview.astra.domain.BookTag;
import xyz.lingview.astra.dto.request.BookCategoryFormDTO;
import xyz.lingview.astra.dto.request.BookFormDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.dto.response.BookViewDTO;
import xyz.lingview.astra.dto.response.ChapterSearchHitDTO;
import xyz.lingview.astra.dto.response.FullTextHitDTO;
import xyz.lingview.astra.dto.response.FullTextRowDTO;
import xyz.lingview.astra.dto.response.ParsedChapterDTO;
import xyz.lingview.astra.mapper.BookMapper;
import xyz.lingview.astra.service.BookService;
import xyz.lingview.astra.service.BookStatsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SORT_OPTIONS = Set.of("updated", "recent_read", "title", "chapters", "words");
    private static final Set<String> SHELF_STATUSES = Set.of("want", "reading", "finished");

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BookStatsService bookStatsService;

    @Autowired
    private xyz.lingview.astra.service.BookChapterService bookChapterService;

    @Override
    public PageResult<BookViewDTO> listBooks(String keyword, Integer categoryId, String shelfStatus, String tagName,
                                             String sort, int pageNum, int pageSize, String userUuid) {
        int page = Math.max(pageNum, 1);
        int size = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        String trimmedKeyword = keyword == null ? null : keyword.trim();
        String keywordPattern = (trimmedKeyword == null || trimmedKeyword.isEmpty())
                ? null : "%" + trimmedKeyword + "%";
        String normalizedSort = SORT_OPTIONS.contains(sort) ? sort : "updated";
        String normalizedShelf = shelfStatus != null && SHELF_STATUSES.contains(shelfStatus) ? shelfStatus : null;
        String normalizedTag = tagName == null || tagName.trim().isEmpty() ? null : tagName.trim();

        int total = bookMapper.countBooks(keywordPattern, categoryId, normalizedShelf, normalizedTag, userUuid);
        List<BookViewDTO> data = bookMapper.selectBooks(keywordPattern, categoryId, normalizedShelf, normalizedTag,
                normalizedSort, userUuid, (page - 1) * size, size);

        PageResult<BookViewDTO> result = new PageResult<>();
        result.setData(data);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setTotal_pages((int) Math.ceil((double) total / size));
        return result;
    }

    @Override
    public List<BookViewDTO> listRecentReading(String userUuid, int limit) {
        List<BookViewDTO> books = bookMapper.selectRecentReading(userUuid, limit);
        for (BookViewDTO book : books) {
            int chapterCount = book.getChapter_count() == null ? 0 : book.getChapter_count();
            int chapterIndex = book.getProgress_chapter_index() == null ? 0 : book.getProgress_chapter_index();
            double scroll = book.getProgress_scroll_percent() == null
                    ? 0 : book.getProgress_scroll_percent().doubleValue();
            double percent = chapterCount > 0
                    ? (chapterIndex + scroll / 100.0) / chapterCount * 100.0
                    : 0;
            percent = Math.max(0, Math.min(100, percent));
            percent = Math.round(percent * 10) / 10.0;
            book.setProgress_percent(percent);
            book.setProgress_percent_text(percent == Math.floor(percent)
                    ? String.valueOf((int) percent)
                    : String.valueOf(percent));
        }
        return books;
    }

    @Override
    public Integer randomBookId() {
        int total = bookMapper.countBooks(null, null, null, null, null);
        if (total <= 0) {
            return null;
        }
        int offset = ThreadLocalRandom.current().nextInt(total);
        return bookMapper.selectBookIdAtOffset(offset);
    }

    @Override
    @Transactional
    public void saveMyReading(Integer bookId, String status, Integer rating, String review, String userUuid) {
        if (bookMapper.selectBook(bookId, null) == null) {
            throw new RuntimeException("图书不存在或已删除");
        }
        if (userUuid == null) {
            throw new RuntimeException("用户未登录");
        }
        if (rating != null && (rating < 0 || rating > 5)) {
            throw new RuntimeException("评分需在 1~5 星之间");
        }

        String normalizedStatus = status != null && SHELF_STATUSES.contains(status) ? status : null;
        LocalDateTime now = LocalDateTime.now();
        String existingStatus = bookMapper.selectShelfStatus(userUuid, bookId);
        if (normalizedStatus == null) {
            if (existingStatus != null) {
                bookMapper.deleteShelf(userUuid, bookId);
            }
        } else if (existingStatus == null) {
            bookMapper.insertShelf(userUuid, bookId, normalizedStatus, now);
        } else {
            bookMapper.updateShelfStatus(userUuid, bookId, normalizedStatus, now);
        }

        int normalizedRating = rating == null ? 0 : rating;
        String trimmedReview = trimToNull(review);
        boolean hasReview = normalizedRating > 0 || trimmedReview != null;
        int reviewCount = bookMapper.countReview(userUuid, bookId);
        if (!hasReview) {
            if (reviewCount > 0) {
                bookMapper.deleteReview(userUuid, bookId);
            }
        } else if (reviewCount == 0) {
            bookMapper.insertReview(userUuid, bookId, normalizedRating > 0 ? normalizedRating : null,
                    trimmedReview, now);
        } else {
            bookMapper.updateReview(userUuid, bookId, normalizedRating > 0 ? normalizedRating : null,
                    trimmedReview, now);
        }
    }

    @Override
    public BookViewDTO getBook(Integer id, String userUuid) {
        return bookMapper.selectBook(id, userUuid);
    }

    @Override
    @Transactional
    public Integer createBook(BookFormDTO dto, String coverUrl) {
        Book book = new Book();
        book.setTitle(dto.getTitle().trim());
        book.setAuthor(trimToNull(dto.getAuthor()));
        book.setCategory_id(dto.getCategory_id());
        book.setDescription(trimToNull(dto.getDescription()));
        book.setIsbn(trimToNull(dto.getIsbn()));
        book.setPublisher(trimToNull(dto.getPublisher()));
        book.setPublish_year(dto.getPublish_year());
        book.setSource_url(trimToNull(dto.getSource_url()));
        book.setCover_url(coverUrl);
        bookMapper.insertBook(book);
        syncTags(book.getId(), dto.getTags());
        return book.getId();
    }

    @Override
    @Transactional
    public Integer createBookWithChapters(BookFormDTO dto, String coverUrl, List<ParsedChapterDTO> chapters) {
        Integer id = createBook(dto, coverUrl);
        if (chapters != null && !chapters.isEmpty()) {
            bookChapterService.importParsedChapters(id, chapters);
        }
        return id;
    }

    @Override
    @Transactional
    public void updateBook(Integer id, BookFormDTO dto, String coverUrl) {
        if (bookMapper.selectBook(id, null) == null) {
            throw new RuntimeException("图书不存在或已删除");
        }
        Book book = new Book();
        book.setId(id);
        book.setTitle(dto.getTitle().trim());
        book.setAuthor(trimToNull(dto.getAuthor()));
        book.setCategory_id(dto.getCategory_id());
        book.setDescription(trimToNull(dto.getDescription()));
        book.setIsbn(trimToNull(dto.getIsbn()));
        book.setPublisher(trimToNull(dto.getPublisher()));
        book.setPublish_year(dto.getPublish_year());
        book.setSource_url(trimToNull(dto.getSource_url()));
        book.setCover_url(coverUrl);
        book.setUpdate_time(LocalDateTime.now());
        bookMapper.updateBook(book);
        syncTags(id, dto.getTags());
    }

    @Override
    public void deleteBook(Integer id) {
        if (bookMapper.softDeleteBook(id) <= 0) {
            throw new RuntimeException("图书不存在或已删除");
        }
    }

    @Override
    public List<BookCategory> listCategories() {
        return bookMapper.selectCategories();
    }

    @Override
    public void createCategory(BookCategoryFormDTO dto) {
        String name = dto.getName().trim();
        if (bookMapper.countCategoryByName(name) > 0) {
            throw new RuntimeException("分类名称已存在");
        }
        BookCategory category = new BookCategory();
        category.setName(name);
        category.setSort_order(dto.getSort_order() == null ? 0 : dto.getSort_order());
        bookMapper.insertCategory(category);
    }

    @Override
    public void renameCategory(Integer id, String name, Integer sortOrder) {
        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            throw new RuntimeException("分类名称不能为空");
        }
        if (bookMapper.countCategoryByNameExcluding(trimmedName, id) > 0) {
            throw new RuntimeException("分类名称已存在");
        }
        if (bookMapper.updateCategory(id, trimmedName, sortOrder == null ? 0 : sortOrder) <= 0) {
            throw new RuntimeException("分类不存在");
        }
    }

    @Override
    public void deleteCategory(Integer id) {
        int bookCount = bookMapper.countBooksByCategory(id);
        if (bookCount > 0) {
            throw new RuntimeException("该分类下还有 " + bookCount + " 本图书，无法删除");
        }
        if (bookMapper.deleteCategory(id) <= 0) {
            throw new RuntimeException("分类不存在");
        }
    }

    @Override
    public BigDecimal touchProgress(Integer bookId, Integer chapterId, String userUuid) {
        BookReadingProgress progress = bookMapper.selectProgress(userUuid, bookId);
        if (progress == null) {
            insertProgress(bookId, chapterId, BigDecimal.ZERO, userUuid);
            bookStatsService.recordChapterOpen(bookId, chapterId, userUuid);
            return BigDecimal.ZERO;
        }
        if (!chapterId.equals(progress.getChapter_id())) {
            bookMapper.updateProgressChapter(userUuid, bookId, chapterId);
            bookStatsService.recordChapterOpen(bookId, chapterId, userUuid);
            return BigDecimal.ZERO;
        }
        return progress.getScroll_percent() == null ? BigDecimal.ZERO : progress.getScroll_percent();
    }

    @Override
    public void saveProgress(Integer bookId, Integer chapterId, BigDecimal scrollPercent, String userUuid) {
        BookReadingProgress progress = bookMapper.selectProgress(userUuid, bookId);
        if (progress == null) {
            insertProgress(bookId, chapterId, scrollPercent, userUuid);
        } else {
            bookMapper.updateProgressScroll(userUuid, bookId, chapterId, scrollPercent);
        }
    }

    private void insertProgress(Integer bookId, Integer chapterId, BigDecimal scrollPercent, String userUuid) {
        BookReadingProgress created = new BookReadingProgress();
        created.setUser_uuid(userUuid);
        created.setBook_id(bookId);
        created.setChapter_id(chapterId);
        created.setScroll_percent(scrollPercent);
        try {
            bookMapper.insertProgress(created);
        } catch (DataIntegrityViolationException e) {
            bookMapper.updateProgressScroll(userUuid, bookId, chapterId, scrollPercent);
        }
    }

    @Override
    public List<BookTag> listTags() {
        return bookMapper.selectAllTags();
    }

    private void syncTags(Integer bookId, String tags) {
        bookMapper.deleteBookTags(bookId);
        if (tags == null) {
            return;
        }
        Set<String> seen = new java.util.LinkedHashSet<>();
        for (String part : tags.split("[,，]")) {
            String name = part.trim();
            if (name.isEmpty()) {
                continue;
            }
            if (name.length() > 50) {
                name = name.substring(0, 50);
            }
            if (!seen.add(name)) {
                continue;
            }
            if (seen.size() > 10) {
                break;
            }
            Integer tagId = bookMapper.selectTagIdByName(name);
            if (tagId == null) {
                BookTag tag = new BookTag();
                tag.setName(name);
                bookMapper.insertTag(tag);
                tagId = tag.getId();
            }
            if (tagId != null) {
                bookMapper.insertBookTag(bookId, tagId);
            }
        }
    }

    @Override
    public List<BookBookmark> listBookmarks(String userUuid, Integer bookId) {
        return bookMapper.selectBookmarks(userUuid, bookId);
    }

    @Override
    public Integer addBookmark(Integer bookId, Integer chapterId, BigDecimal scrollPercent, String note,
                               String userUuid) {
        Integer ownerBookId = bookMapper.selectChapterBookId(chapterId);
        if (ownerBookId == null || !ownerBookId.equals(bookId)) {
            throw new RuntimeException("章节不存在或不属于该图书");
        }
        BigDecimal percent = scrollPercent == null ? BigDecimal.ZERO : scrollPercent;
        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            percent = BigDecimal.ZERO;
        }
        BigDecimal hundred = new BigDecimal("100");
        if (percent.compareTo(hundred) > 0) {
            percent = hundred;
        }
        String trimmedNote = trimToNull(note);
        if (trimmedNote != null && trimmedNote.length() > 200) {
            trimmedNote = trimmedNote.substring(0, 200);
        }
        bookMapper.insertBookmark(userUuid, bookId, chapterId, percent, trimmedNote, LocalDateTime.now());
        return bookMapper.selectLatestBookmarkId(userUuid, bookId, chapterId);
    }

    @Override
    public void deleteBookmark(Integer id, String userUuid) {
        bookMapper.deleteBookmark(id, userUuid);
    }

    @Override
    public List<ChapterSearchHitDTO> searchInBook(Integer bookId, String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        List<BookChapter> chapters = bookMapper.searchChapters(bookId, "%" + trimmed + "%");
        List<ChapterSearchHitDTO> hits = new java.util.ArrayList<>(chapters.size());
        String lowerKeyword = trimmed.toLowerCase();
        for (BookChapter chapter : chapters) {
            ChapterSearchHitDTO hit = new ChapterSearchHitDTO();
            hit.setChapter_id(chapter.getId());
            hit.setTitle(chapter.getTitle());
            hit.setSnippet(buildSnippet(chapter.getContent(), lowerKeyword, 30, 50, chapter.getTitle()));
            hits.add(hit);
        }
        return hits;
    }

    @Override
    public List<FullTextHitDTO> searchAll(String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        List<FullTextRowDTO> rows = bookMapper.searchAllChapters("%" + trimmed + "%");
        List<FullTextHitDTO> hits = new java.util.ArrayList<>(rows.size());
        String lowerKeyword = trimmed.toLowerCase();
        for (FullTextRowDTO row : rows) {
            FullTextHitDTO hit = new FullTextHitDTO();
            hit.setBook_id(row.getBook_id());
            hit.setBook_title(row.getBook_title());
            hit.setChapter_id(row.getChapter_id());
            hit.setChapter_title(row.getChapter_title());
            hit.setSnippet(buildSnippet(row.getContent(), lowerKeyword, 40, 80, row.getChapter_title()));
            hits.add(hit);
        }
        return hits;
    }

    private String buildSnippet(String content, String lowerKeyword, int before, int after, String fallback) {
        String text = content == null ? "" : content;
        int index = text.toLowerCase().indexOf(lowerKeyword);
        if (index < 0) {
            return fallback;
        }
        int start = Math.max(0, index - before);
        int end = Math.min(text.length(), index + lowerKeyword.length() + after);
        String snippet = text.substring(start, end).replaceAll("\\s+", " ");
        if (start > 0) {
            snippet = "…" + snippet;
        }
        if (end < text.length()) {
            snippet = snippet + "…";
        }
        return snippet;
    }

    @Override
    public List<BookNote> listNotes(String userUuid, Integer bookId) {
        if (userUuid == null) {
            return List.of();
        }
        return bookMapper.selectNotes(userUuid, bookId);
    }

    @Override
    public void addNote(Integer bookId, Integer chapterId, String quoteText, String noteText, String userUuid) {
        if (userUuid == null) {
            throw new RuntimeException("用户未登录");
        }
        Integer ownerBookId = bookMapper.selectChapterBookId(chapterId);
        if (ownerBookId == null || !ownerBookId.equals(bookId)) {
            throw new RuntimeException("章节不存在或不属于该图书");
        }
        String quote = trimToNull(quoteText);
        String note = trimToNull(noteText);
        if (quote == null && note == null) {
            throw new RuntimeException("划线或想法至少填写一项");
        }
        if (quote != null && quote.length() > 2000) {
            quote = quote.substring(0, 2000);
        }
        if (note != null && note.length() > 2000) {
            note = note.substring(0, 2000);
        }
        bookMapper.insertNote(userUuid, bookId, chapterId, quote, note, LocalDateTime.now());
    }

    @Override
    public void deleteNote(Integer id, String userUuid) {
        bookMapper.deleteNote(id, userUuid);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}