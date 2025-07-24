package com.savit.budget.service;

import com.savit.budget.domain.BudgetCategoryVO;
import com.savit.budget.dto.BudgetCategoryDTO;

import java.util.List;
import java.util.Map;

public interface BudgetCategoryService {
    void createBudgetCategory(List<BudgetCategoryDTO> categories, Long userId);

    int changeBudgetCategory(List<BudgetCategoryDTO> categories, Long userId);

    Map<String, List<BudgetCategoryVO>> getBudgetCategories(Long userId, List<String> months, List<Long> categoryIds);
}
