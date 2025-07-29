package com.savit.challenge.controller;

import com.savit.challenge.dto.ChallengeListDTO;
import com.savit.challenge.service.ChallengeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/challenge")
@RequiredArgsConstructor
@Slf4j
public class ChallengeController {

    private  final ChallengeService challengeService;
    @GetMapping
    public ResponseEntity<List<ChallengeListDTO>> getChallengeList () {
        List<ChallengeListDTO> result = challengeService.getChallengeList();
        return ResponseEntity.ok(result);
    }
}
