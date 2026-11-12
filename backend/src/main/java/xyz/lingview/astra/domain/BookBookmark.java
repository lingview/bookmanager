package xyz.lingview.astra.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookBookmark {
    private Integer id;
    private String user_uuid;
    private Integer book_id;
    private Integer chapter_id;
    private String chapter_title;
    private BigDecimal scroll_percent;
    private String note;
    private String create_time;
}
