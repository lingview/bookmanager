package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookFormDTO {

    @NotBlank(message = "书名不能为空")
    @Size(max = 255, message = "书名不能超过255个字符")
    private String title;

    @Size(max = 255, message = "作者不能超过255个字符")
    private String author;

    private Integer category_id;

    @Size(max = 2000, message = "简介不能超过2000个字符")
    private String description;

    private String cover_url;
}