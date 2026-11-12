package xyz.lingview.astra.dto.response;

import lombok.Data;

@Data
public class PreviewChapterContentDTO {
    private int index;
    private String title;
    private String content;
    private int word_count;
}
