package xyz.lingview.astra.dto.response;

import lombok.Data;

@Data
public class CategoryStatDTO {
    private String name;
    private int book_count;
    private int chapter_count;
    private long word_count;
}