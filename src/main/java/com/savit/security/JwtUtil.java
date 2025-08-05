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
     * HttpServletRequest에서 userId 추출
     */
    public Long getUserIdFromToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        if (token == null) {
            throw new JwtTokenException("토큰이 없습니다.");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new JwtTokenException("유효하지 않은 토큰입니다.");
        }

        if (!jwtTokenProvider.isAccessToken(token)) {
            throw new JwtTokenException("Access Token이 아닙니다.");
        }

        String userIdStr = jwtTokenProvider.getUserId(token);
        return Long.parseLong(userIdStr);
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
     * 토큰에서 userId만 추출 (Request 없이)
     */
    public Long getUserIdFromToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new JwtTokenException("유효하지 않은 토큰입니다.");
        }

        String userIdStr = jwtTokenProvider.getUserId(token);
        return Long.parseLong(userIdStr);
    }
}