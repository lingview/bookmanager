package xyz.lingview.astra.dto.response;

import lombok.Data;

@Data
public class FinishedBookDTO {
    private Integer id;
    private String title;
    private String author;
    private String finish_time;
}
