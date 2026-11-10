package xyz.lingview.astra.domain;

import lombok.Data;

@Data
public class BookCategory {
    private Integer id;
    private String name;
    private Integer sort_order;
    private Integer status;
}