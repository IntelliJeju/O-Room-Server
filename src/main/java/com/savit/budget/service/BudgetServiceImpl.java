package com.savit.budget.service;

import com.savit.budget.domain.BudgetVO;
import com.savit.budget.dto.BudgetDTO;
import com.savit.budget.mapper.BudgetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BudgetServiceImpl implements BudgetService{

    private final BudgetMapper budgetMapper;

    @Override
    public void createBudget(BudgetDTO dto, Long userId) {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        BudgetVO vo= BudgetVO.builder()
                .userId(userId)
                .month(currentMonth)
                .totalBudget(dto.getTotalBudget())
                .build();

        budgetMapper.insertBudget(vo);
    }

    @Override
    public int changeBudget(BudgetDTO budgetDTO, Long userId) {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        BudgetVO vo = BudgetVO.builder()
                .userId(userId)
                .month(currentMonth)
                .totalBudget(budgetDTO.getTotalBudget()).build();
       return budgetMapper.updateBudget(vo);
    }

    @Override
    public BudgetVO getBudget(Long userId) {
        String curMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return budgetMapper.getBudget(userId,curMonth);
    }
}
