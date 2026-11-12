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

    @Size(max = 32, message = "ISBN 不能超过32个字符")
    private String isbn;

    @Size(max = 100, message = "出版社不能超过100个字符")
    private String publisher;

    private Integer publish_year;

    @Size(max = 500, message = "来源链接不能超过500个字符")
    private String source_url;

    @Size(max = 500, message = "标签总长度不能超过500个字符")
    private String tags;

    private String cover_url;
}