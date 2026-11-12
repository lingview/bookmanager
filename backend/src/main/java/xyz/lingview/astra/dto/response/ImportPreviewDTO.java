package xyz.lingview.astra.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ImportPreviewDTO {
    private String token;
    private String filename;
    private int chapter_count;
    private int total_words;
    private List<PreviewChapterSummaryDTO> chapters;
}
