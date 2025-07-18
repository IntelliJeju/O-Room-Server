package com.oroom.user.controller;

import com.oroom.user.service.AuthService;
import com.oroom.user.dto.LoginResponseDTO;
import com.oroom.security.KakaoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final KakaoProperties kakaoProperties;

    // 카카오 로그인 시작
    @GetMapping("/kakao")
    public String kakaoLogin() {
        try {
            String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize" +
                    "?response_type=code" +
                    "&client_id=" + kakaoProperties.getClientId() +
                    "&redirect_uri=" + URLEncoder.encode(kakaoProperties.getRedirectUri(), "UTF-8");

            log.info("카카오 인증 URL로 리다이렉트: {}", kakaoAuthUrl);
            return "redirect:" + kakaoAuthUrl;
        } catch (Exception e) {
            log.error("카카오 인증 URL 생성 실패", e);
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/login/kakao")
    @ResponseBody
    public ResponseEntity<?> kakaoCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            HttpServletResponse response) {

        log.info("카카오 콜백 호출됨. code: {}, error: {}", code, error);

        // 에러 처리
        if (error != null) {
            log.error("카카오 인증 에러: {}", error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("카카오 인증이 취소되었습니다.");
        }

        // code 파라미터 체크
        if (code == null || code.trim().isEmpty()) {
            log.error("인가 코드가 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("인가 코드가 필요합니다.");
        }

        try {
            log.info("=== 카카오 로그인 처리 시작 ===");

            // 카카오 로그인 처리
            LoginResponseDTO loginResponse = authService.kakaoLogin(code);

            log.info("=== 카카오 로그인 처리 완료 ===");

            // JWT 토큰을 헤더에 설정
            response.setHeader("Authorization", "Bearer " + loginResponse.getToken());

            log.info("카카오 로그인 성공. 사용자: {}", loginResponse.getUser().getEmail());

            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            log.error("카카오 로그인 처리 실패 - 상세 오류:", e);  // 이 부분이 중요!
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}