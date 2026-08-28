package xyz.lingview.astra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiKeyCreatedResponseDTO {
    private Integer id;
    private String description;
    private String apiKey;
}