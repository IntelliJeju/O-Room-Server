package com.savit.budget.controller;

import com.savit.budget.dto.BudgetDTO;
import com.savit.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/budget")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {
    private final BudgetService budgetService;
    // private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Void> createBudget(@RequestBody BudgetDTO dto, HttpServletRequest request){
       // Long userId = jwtUtil.getUserIdFromToken(request);
       // budgetService.createBudget(dto, userId);
        return ResponseEntity.ok().build();
    }

    // @PutMapping
   // public int changeBudget(@RequestBody BudgetDTO dto, HttpServletRequest request) {
       // Long userId = jwtUtil.getUserIdFromToken(request);
        // budgetService.changeBudget(dto,userId);
       // return}
}
