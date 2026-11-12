package xyz.lingview.astra.dto.response;

import lombok.Data;

@Data
public class ChapterSearchHitDTO {
    private Integer chapter_id;
    private String title;
    private String snippet;
}
