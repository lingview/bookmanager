package xyz.lingview.astra.dto.response;

import lombok.Data;

@Data
public class HomeOverviewDTO {
    private long week_seconds;
    private long total_seconds;
    private int streak_days;
    private int reading_book_count;
    private int finished_this_year;
    private int year_goal;
    private String week_text;
    private String total_text;
}
