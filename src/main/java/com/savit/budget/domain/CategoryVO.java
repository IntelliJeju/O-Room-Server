package com.savit.budget.domain;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id; // 카테고리 ID
    private String name; // 카테고리 이름
    private String industryName; // 업종명
}
