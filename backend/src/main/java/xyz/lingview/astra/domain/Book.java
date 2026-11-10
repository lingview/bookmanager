package xyz.lingview.astra.domain;

import lombok.Data;

@Data
public class Book {
    private Integer id;
    private String title;
    private String author;
    private Integer category_id;
    private String cover_url;
    private String description;
    private Integer status;
}