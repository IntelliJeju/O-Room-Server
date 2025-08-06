package com.savit.challenge.controller;

import com.savit.challenge.dto.ChallengeDetailDTO;
import com.savit.challenge.dto.ChallengeListDTO;
import com.savit.challenge.dto.ChallengeStatusDTO;
import com.savit.challenge.service.ChallengeService;

import com.savit.challenge.service.ChallengeStatusService;
import com.savit.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/challenge")
@RequiredArgsConstructor
@Slf4j
public class ChallengeController {

    private  final ChallengeService challengeService;
    private final ChallengeStatusService challengeStatusService;
    private final JwtUtil jwtUtil;

    @GetMapping("/available")
    public ResponseEntity<List<ChallengeListDTO>> getChallengeList (HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        List<ChallengeListDTO> result = challengeService.getChallengeList(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/available/{challenge_id}")
    public ResponseEntity<ChallengeDetailDTO> getChallengeDetail(@PathVariable Long challenge_id, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        ChallengeDetailDTO result = challengeService.getChallengeDetail(challenge_id, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/participating")
    public ResponseEntity<List<ChallengeListDTO>> getParticipatingChallenges (HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        List<ChallengeListDTO> result = challengeService.getParticipatingChallenges(userId);
        return ResponseEntity.ok(result);

    }

    @GetMapping("/participating/{challenge_id}")
    public ResponseEntity<ChallengeStatusDTO> getChallengeStatus(@PathVariable Long challenge_id, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(request);
        ChallengeStatusDTO result = challengeStatusService.getChallengeStatus(challenge_id, userId);
        return ResponseEntity.ok(result);
    }
}
