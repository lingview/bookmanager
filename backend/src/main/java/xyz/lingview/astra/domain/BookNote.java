package xyz.lingview.astra.domain;

import lombok.Data;

@Data
public class BookNote {
    private Integer id;
    private String user_uuid;
    private Integer book_id;
    private Integer chapter_id;
    private String chapter_title;
    private String quote_text;
    private String note_text;
    private String create_time;
}
