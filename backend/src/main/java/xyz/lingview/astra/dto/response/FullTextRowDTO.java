package xyz.lingview.astra.dto.response;

import lombok.Data;

@Data
public class FullTextRowDTO {
    private Integer book_id;
    private String book_title;
    private Integer chapter_id;
    private String chapter_title;
    private String content;
}
