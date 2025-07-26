package com.savit.budget.mapper;

import com.savit.budget.domain.CategoryVO;

public interface CategoryMapper {
    CategoryVO findById(Long categoryId);
    CategoryVO findByName(String name);
}
