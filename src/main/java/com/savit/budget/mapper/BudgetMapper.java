package com.savit.budget.mapper;

import com.savit.budget.domain.BudgetVO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface BudgetMapper {
    void insertBudget(BudgetVO vo);
    int updateBudget(BudgetVO vo);
}
