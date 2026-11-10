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
    private String update_time;
    private Integer chapter_count;
    private Integer progress_chapter_id;
    private String progress_chapter_title;
    private BigDecimal progress_scroll_percent;
}