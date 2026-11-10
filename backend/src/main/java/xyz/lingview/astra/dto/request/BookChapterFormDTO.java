package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookChapterFormDTO {

    @NotBlank(message = "章节标题不能为空")
    @Size(max = 255, message = "章节标题不能超过255个字符")
    private String title;

    @NotBlank(message = "章节正文不能为空")
    private String content;
}