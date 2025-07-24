package com.savit.budget.service;

import com.savit.budget.domain.BudgetCategoryVO;
import com.savit.budget.domain.BudgetVO;
import com.savit.budget.dto.BudgetCategoryDTO;
import com.savit.budget.mapper.BudgetCategoryMapper;
import com.savit.budget.mapper.BudgetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BudgetCategoryServiceImpl implements BudgetCategoryService{

    private final BudgetMapper budgetMapper;
    private final BudgetCategoryMapper budgetCategoryMapper;

    @Override
    public void createBudgetCategory(List<BudgetCategoryDTO> categories, Long userId) {

        // budgetId 가져오기
        String curMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        BudgetVO budget = budgetMapper.getBudget(userId,curMonth);

        if (budget == null ) {
            log.warn("예산 미설정: userid={},month={}", userId,curMonth);
            throw new NoSuchElementException("해당 월의 예산이 설정되지 않았음");
        }
        // 예산 초과 검증하기(total_budget >= sum.target_amount)
        BigDecimal totalCategoryBudget = categories.stream()
                .map(BudgetCategoryDTO::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if(totalCategoryBudget.compareTo(budget.getTotalBudget()) > 0 ) {
            log.warn("카테고리별 예산: {} 이 월별 예산 초과: {} ", totalCategoryBudget,budget.getTotalBudget());
            throw new IllegalArgumentException("총예산 초과");
        }

        // budgetcategory 저장
        for (BudgetCategoryDTO dto : categories) {
            BudgetCategoryVO vo = BudgetCategoryVO.builder()
                    .budgetId(budget.getId())
                    .categoryId(dto.getCategoryId())
                    .targetAmount(dto.getTargetAmount()).build();
            budgetCategoryMapper.insertBudgetCategory(vo);
        }
    }

    // create과 유사
    @Override
    public int changeBudgetCategory(List<BudgetCategoryDTO> categories, Long userId) {
        String curMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        BudgetVO budget = budgetMapper.getBudget(userId, curMonth);

        if(budget == null ) {
            log.warn("총예산 설정되어 있지 않음");
            throw new NoSuchElementException("해당 월의 예산이 설정되어 있지 않음");
        }

        BigDecimal totalCategoryBudget = categories.stream().map(BudgetCategoryDTO::getTargetAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(totalCategoryBudget.compareTo(budget.getTotalBudget())> 0) {
            log.warn("카테고리별 예산: {} 이 월별 예산 초과: {} ", totalCategoryBudget, budget.getTotalBudget());
            throw new IllegalArgumentException("총예산초과");
        }

        // budgetcategory 수정 저장
        int totalUpdated = 0;
        for (BudgetCategoryDTO dto : categories) {
            BudgetCategoryVO vo = BudgetCategoryVO.builder()
                    .budgetId(budget.getId())
                    .categoryId(dto.getCategoryId())
                    .targetAmount(dto.getTargetAmount()).build();
          int result =  budgetCategoryMapper.updateBudgetCategory(vo);
          totalUpdated += result;
        }
        return  totalUpdated;


    }

    @Override
    public Map<String, List<BudgetCategoryVO>> getBudgetCategories(Long userId, List<String> months, List<Long> categoryIds) {
        // months가 null이면 현재월만 사용
        if (months == null || months.isEmpty()) {
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            months = Arrays.asList(currentMonth);
        }

        Map<String, List<BudgetCategoryVO>> result = new HashMap<>();

        for (String month : months) {
            // 해당 월의 budget 조회해서 budgetId 얻기
            BudgetVO budget = budgetMapper.getBudget(userId, month);
            if (budget == null) {
                log.warn("예산 미설정: userId={}, month={}", userId, month);
                result.put(month, new ArrayList<>());
                continue;
            }

            // budgetCategory 조회
            List<BudgetCategoryVO> categories = budgetCategoryMapper.getBudgetCategories(budget.getId(), categoryIds);
            result.put(month, categories);
        }

        return result;
    }
}
