package xyz.lingview.astra.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Book {
    private Integer id;
    private String title;
    private String author;
    private Integer category_id;
    private String cover_url;
    private String description;
    private String isbn;
    private String publisher;
    private Integer publish_year;
    private String source_url;
    private Integer status;
    private LocalDateTime update_time;
}