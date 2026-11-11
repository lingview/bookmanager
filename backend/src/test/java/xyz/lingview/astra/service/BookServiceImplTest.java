package xyz.lingview.astra.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.lingview.astra.domain.BookReadingProgress;
import xyz.lingview.astra.dto.request.BookCategoryFormDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.dto.response.BookViewDTO;
import xyz.lingview.astra.mapper.BookMapper;
import xyz.lingview.astra.service.impl.BookServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void listBooks_shouldNormalizePagination() {
        when(bookMapper.countBooks(isNull(), isNull())).thenReturn(25);
        when(bookMapper.selectBooks(isNull(), isNull(), eq("u1"), eq(0), eq(12))).thenReturn(List.of());

        PageResult<BookViewDTO> result = bookService.listBooks(null, null, 0, 0, "u1");

        assertEquals(1, result.getPage());
        assertEquals(12, result.getSize());
        assertEquals(25, result.getTotal());
        assertEquals(3, result.getTotal_pages());
    }

    @Test
    void listBooks_shouldCapPageSize() {
        when(bookMapper.countBooks(eq("%x%"), isNull())).thenReturn(0);
        when(bookMapper.selectBooks(eq("%x%"), isNull(), eq("u1"), eq(0), eq(100))).thenReturn(List.of());

        PageResult<BookViewDTO> result = bookService.listBooks("  x  ", null, 1, 500, "u1");

        assertEquals(100, result.getSize());
    }

    @Test
    void deleteCategory_shouldRejectWhenBooksExist() {
        when(bookMapper.countBooksByCategory(3)).thenReturn(2);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookService.deleteCategory(3));

        assertTrue(ex.getMessage().contains("2"));
        verify(bookMapper, never()).deleteCategory(any());
    }

    @Test
    void deleteCategory_shouldDeleteWhenEmpty() {
        when(bookMapper.countBooksByCategory(3)).thenReturn(0);
        when(bookMapper.deleteCategory(3)).thenReturn(1);

        bookService.deleteCategory(3);

        verify(bookMapper).deleteCategory(3);
    }

    @Test
    void createCategory_shouldRejectDuplicateName() {
        BookCategoryFormDTO dto = new BookCategoryFormDTO();
        dto.setName(" 文学 ");
        when(bookMapper.countCategoryByName("文学")).thenReturn(1);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookService.createCategory(dto));

        assertEquals("分类名称已存在", ex.getMessage());
    }

    @Test
    void touchProgress_shouldCreateWhenMissing() {
        when(bookMapper.selectProgress("u1", 1)).thenReturn(null);

        BigDecimal restore = bookService.touchProgress(1, 10, "u1");

        assertEquals(BigDecimal.ZERO, restore);
        verify(bookMapper).insertProgress(any(BookReadingProgress.class));
    }

    @Test
    void touchProgress_shouldKeepScrollInSameChapter() {
        BookReadingProgress progress = new BookReadingProgress();
        progress.setChapter_id(10);
        progress.setScroll_percent(new BigDecimal("42.50"));
        when(bookMapper.selectProgress("u1", 1)).thenReturn(progress);

        BigDecimal restore = bookService.touchProgress(1, 10, "u1");

        assertEquals(new BigDecimal("42.50"), restore);
        verify(bookMapper, never()).updateProgressChapter(any(), any(), any());
    }

    @Test
    void touchProgress_shouldResetScrollWhenChapterChanges() {
        BookReadingProgress progress = new BookReadingProgress();
        progress.setChapter_id(10);
        progress.setScroll_percent(new BigDecimal("42.50"));
        when(bookMapper.selectProgress("u1", 1)).thenReturn(progress);

        BigDecimal restore = bookService.touchProgress(1, 11, "u1");

        assertEquals(BigDecimal.ZERO, restore);
        verify(bookMapper).updateProgressChapter("u1", 1, 11);
    }
}