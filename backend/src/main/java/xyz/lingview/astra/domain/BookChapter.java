package xyz.lingview.astra.domain;

import lombok.Data;

@Data
public class BookChapter {
    private Integer id;
    private Integer book_id;
    private String title;
    private String content;
    private Integer sort_order;
    private Integer status;
}