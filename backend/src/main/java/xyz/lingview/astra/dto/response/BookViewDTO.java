package xyz.lingview.astra.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookViewDTO {
    private Integer id;
    private String title;
    private String author;
    private Integer category_id;
    private String category_name;
    private String cover_url;
    private String description;
    private String isbn;
    private String publisher;
    private Integer publish_year;
    private String source_url;
    private String tag_names;
    private String update_time;
    private String shelf_status;
    private Integer my_rating;
    private String my_review;
    private Integer chapter_count;
    private Long word_total;
    private Integer progress_chapter_id;
    private String progress_chapter_title;
    private BigDecimal progress_scroll_percent;
    private Integer progress_chapter_index;
    private Double progress_percent;
    private String progress_percent_text;
}