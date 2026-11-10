package xyz.lingview.astra.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookProgressDTO {

    @NotNull(message = "章节ID不能为空")
    private Integer chapter_id;

    @DecimalMin(value = "0.00", message = "阅读位置不能小于0")
    @DecimalMax(value = "100.00", message = "阅读位置不能大于100")
    private BigDecimal scroll_percent;
}