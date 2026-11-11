package xyz.lingview.astra.dto.response;

import lombok.Data;

@Data
public class BookWordStatDTO {
    private String title;
    private long word_count;
    private int chapter_count;
}