package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ChapterOrderDTO {

    @NotEmpty(message = "章节顺序不能为空")
    private List<Integer> ids;
}