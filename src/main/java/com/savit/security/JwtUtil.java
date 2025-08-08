package com.savit.security;

import com.savit.common.exception.JwtTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * HttpServletRequest에서 userId 추출 (안전한 버전)
     */
    public Long getUserIdFromToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        if (token == null) {
            throw new JwtTokenException("토큰이 없습니다.");
        }

        // 토큰 상태 종합 검증
        JwtTokenProvider.TokenValidationResult validationResult =
                jwtTokenProvider.validateTokenWithDetails(token);

        if (!validationResult.isValid()) {
            if (validationResult.isExpired()) {
                throw new JwtTokenException("토큰이 만료되었습니다.");
            } else {
                throw new JwtTokenException("유효하지 않은 토큰입니다.");
            }
        }

        // Access Token 확인 (만료된 토큰도 안전하게 처리)
        if (!jwtTokenProvider.isAccessToken(token)) {
            throw new JwtTokenException("Access Token이 아닙니다.");
        }

        // 사용자 ID 추출 (만료된 토큰도 안전하게 처리)
        String userIdStr = jwtTokenProvider.getUserId(token);
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new JwtTokenException("토큰에서 사용자 ID를 추출할 수 없습니다.");
        }

        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new JwtTokenException("토큰의 사용자 ID 형식이 잘못되었습니다.");
        }
    }

    /**
     * Request Header에서 토큰 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * 토큰에서 userId만 추출 (Request 없이) - 안전한 버전
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtTokenException("토큰이 없습니다.");
        }

        // 토큰 상태 검증
        JwtTokenProvider.TokenValidationResult validationResult =
                jwtTokenProvider.validateTokenWithDetails(token);

        if (!validationResult.isValid()) {
            throw new JwtTokenException(validationResult.getMessage());
        }

        String userIdStr = jwtTokenProvider.getUserId(token);
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new JwtTokenException("토큰에서 사용자 ID를 추출할 수 없습니다.");
        }

        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new JwtTokenException("토큰의 사용자 ID 형식이 잘못되었습니다.");
        }
    }

    /**
     * 만료된 토큰에서도 userId 추출 (특별한 경우용)
     */
    public Long getUserIdFromExpiredToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtTokenException("토큰이 없습니다.");
        }

        try {
            String userIdStr = jwtTokenProvider.getUserId(token); // 만료된 토큰도 처리 가능
            if (userIdStr == null || userIdStr.isBlank()) {
                throw new JwtTokenException("토큰에서 사용자 ID를 추출할 수 없습니다.");
            }
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new JwtTokenException("토큰의 사용자 ID 형식이 잘못되었습니다.");
        }
    }

    /**
     * 토큰 유효성 단순 체크 (예외 없이 boolean만 반환)
     */
    public boolean isValidToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 토큰 만료 여부 체크 (예외 없이 boolean만 반환)
     */
    public boolean isTokenExpired(String token) {
        return jwtTokenProvider.isTokenExpired(token);
    }
}