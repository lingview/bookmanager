package xyz.lingview.astra.dto.response;

import lombok.Data;

@Data
public class DailyReadingDTO {
    private String stat_date;
    private long seconds;
}