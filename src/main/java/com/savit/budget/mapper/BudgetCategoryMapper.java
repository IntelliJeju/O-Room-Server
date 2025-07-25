package com.savit.budget.mapper;

import com.savit.budget.domain.BudgetCategoryVO;
import com.savit.budget.dto.BudgetCategoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BudgetCategoryMapper {
    void insertBudgetCategory(BudgetCategoryVO vo);

    int updateBudgetCategory(BudgetCategoryVO vo);

    List<BudgetCategoryVO> getBudgetCategories(@Param("budgetId") Long budgetId, @Param("categoryIds") List<Long> categoryIds);
}