package xyz.lingview.astra.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookReadingProgress {
    private String user_uuid;
    private Integer book_id;
    private Integer chapter_id;
    private BigDecimal scroll_percent;
}