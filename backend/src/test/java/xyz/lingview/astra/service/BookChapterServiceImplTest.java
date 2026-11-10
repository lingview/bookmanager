package xyz.lingview.astra.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import xyz.lingview.astra.domain.BookChapter;
import xyz.lingview.astra.dto.response.ChapterSummaryDTO;
import xyz.lingview.astra.mapper.BookChapterMapper;
import xyz.lingview.astra.service.impl.BookChapterServiceImpl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookChapterServiceImplTest {

    @Mock
    private BookChapterMapper chapterMapper;

    @InjectMocks
    private BookChapterServiceImpl chapterService;

    @Test
    void getChapter_shouldRejectChapterOfAnotherBook() {
        BookChapter chapter = new BookChapter();
        chapter.setId(5);
        chapter.setBook_id(2);
        when(chapterMapper.selectChapter(5)).thenReturn(chapter);

        assertNull(chapterService.getChapter(1, 5));
    }

    @Test
    void getChapter_shouldReturnOwnedChapter() {
        BookChapter chapter = new BookChapter();
        chapter.setId(5);
        chapter.setBook_id(1);
        when(chapterMapper.selectChapter(5)).thenReturn(chapter);

        assertNotNull(chapterService.getChapter(1, 5));
    }

    @Test
    void reorder_shouldRejectMismatchedIds() {
        when(chapterMapper.selectChapterSummaries(1)).thenReturn(List.of(summary(1), summary(2)));

        assertThrows(RuntimeException.class, () -> chapterService.reorder(1, List.of(1, 3)));
    }

    @Test
    void reorder_shouldRewriteSortOrder() {
        when(chapterMapper.selectChapterSummaries(1)).thenReturn(List.of(summary(1), summary(2)));

        chapterService.reorder(1, List.of(2, 1));

        verify(chapterMapper).updateChapterSort(2, 10);
        verify(chapterMapper).updateChapterSort(1, 20);
    }

    @Test
    void importChapters_shouldSplitByHeading() {
        String text = "序言部分\n第一章 开端\n内容一\n第二章 发展\n内容二\n";
        MockMultipartFile file = textFile("book.txt", text.getBytes(StandardCharsets.UTF_8));

        int count = chapterService.importChapters(1, file);

        assertEquals(2, count);
        ArgumentCaptor<BookChapter> captor = ArgumentCaptor.forClass(BookChapter.class);
        verify(chapterMapper, times(2)).insertChapter(captor.capture());
        List<BookChapter> saved = captor.getAllValues();
        assertEquals("第一章 开端", saved.get(0).getTitle());
        assertTrue(saved.get(0).getContent().contains("序言部分"));
        assertTrue(saved.get(0).getContent().contains("内容一"));
        assertEquals("第二章 发展", saved.get(1).getTitle());
        assertEquals(Integer.valueOf(10), saved.get(0).getSort_order());
        assertEquals(Integer.valueOf(20), saved.get(1).getSort_order());
    }

    @Test
    void importChapters_shouldFallbackToWholeFile() {
        String text = "只有一段内容\n没有章节标题\n";
        MockMultipartFile file = textFile("整本书.txt", text.getBytes(StandardCharsets.UTF_8));

        int count = chapterService.importChapters(1, file);

        assertEquals(1, count);
        ArgumentCaptor<BookChapter> captor = ArgumentCaptor.forClass(BookChapter.class);
        verify(chapterMapper).insertChapter(captor.capture());
        assertEquals("整本书", captor.getValue().getTitle());
        assertTrue(captor.getValue().getContent().contains("只有一段内容"));
    }

    @Test
    void importChapters_shouldDecodeGbk() {
        String text = "第一章 你好\n中文内容\n";
        MockMultipartFile file = textFile("gbk.txt", text.getBytes(Charset.forName("GBK")));

        int count = chapterService.importChapters(1, file);

        assertEquals(1, count);
        ArgumentCaptor<BookChapter> captor = ArgumentCaptor.forClass(BookChapter.class);
        verify(chapterMapper).insertChapter(captor.capture());
        assertTrue(captor.getValue().getTitle().contains("第一章"));
        assertTrue(captor.getValue().getContent().contains("中文内容"));
    }

    @Test
    void importChapters_shouldRejectEmptyFile() {
        MockMultipartFile file = textFile("empty.txt", new byte[0]);

        assertThrows(RuntimeException.class, () -> chapterService.importChapters(1, file));
    }

    @Test
    void importChapters_shouldRejectNonTxt() {
        MockMultipartFile file = textFile("book.md", "内容".getBytes(StandardCharsets.UTF_8));

        assertThrows(RuntimeException.class, () -> chapterService.importChapters(1, file));
    }

    private MockMultipartFile textFile(String filename, byte[] content) {
        return new MockMultipartFile("file", filename, "text/plain", content);
    }

    private ChapterSummaryDTO summary(int id) {
        ChapterSummaryDTO summary = new ChapterSummaryDTO();
        summary.setId(id);
        return summary;
    }
}