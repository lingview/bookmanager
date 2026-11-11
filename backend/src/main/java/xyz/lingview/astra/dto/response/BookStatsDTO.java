package xyz.lingview.astra.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BookStatsDTO {
    private int book_total;
    private int book_active;
    private int book_deleted;
    private int category_total;
    private int chapter_total;
    private long word_total;
    private int avg_chapter_words;
    private long reading_seconds_total;
    private int reader_count;
    private int book_read_count;
    private double complete_rate;
    private List<CategoryStatDTO> categories;
    private List<BookWordStatDTO> top_books;
    private List<BucketStatDTO> chapter_buckets;
    private List<DailyReadingDTO> daily_reading;
}