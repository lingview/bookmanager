package xyz.lingview.astra.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PersonalStatsDTO {
    private int year;
    private long total_seconds;
    private String total_text;
    private long week_seconds;
    private String week_text;
    private int days_count;
    private int streak_days;
    private int finished_count;
    private int year_finished;
    private int year_goal;
    private List<FinishedBookDTO> finished_books;
    private List<DailyReadingDTO> daily;
    private List<PersonalCategoryDTO> categories;
}
