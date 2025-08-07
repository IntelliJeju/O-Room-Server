package com.savit.security;

import com.savit.common.exception.JwtTokenException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenValidityInMilliseconds;  // 10분

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidityInMilliseconds; // 7일

    // access token 생성
    public String createAccessToken(String userId) {
        Claims claims = Jwts.claims().setSubject(userId);
        // 토큰 타입 구분
        claims.put("type", "access");

        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // refresh token 생성
    public String createRefreshToken(String userId) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("type", "refresh"); // 토큰 타입 구분

        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // refresh & access token 동시 생성
    public TokenPairDTO createTokenPair(String userId) {
        String accessToken = createAccessToken(userId);
        String refreshToken = createRefreshToken(userId);

        return TokenPairDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 토큰에서 userid 추출 (만료된 토큰도 처리)
    public String getUserId(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서도 userId 추출 가능
            return e.getClaims().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtTokenException("유효하지 않은 토큰입니다");
        }
    }

    // 토큰 유효성 검증 (만료 여부만 체크, 예외 던지지 않음)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;  // 만료된 토큰은 false 반환
        } catch (JwtException | IllegalArgumentException e) {
            return false;  // 유효하지 않은 토큰은 false 반환
        }
    }

    // 토큰이 만료되었는지 확인 (만료 여부만 체크)
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return false;  // 파싱 성공하면 만료되지 않음
        } catch (ExpiredJwtException e) {
            return true;   // 만료된 토큰
        } catch (JwtException | IllegalArgumentException e) {
            return true;   // 유효하지 않은 토큰도 만료로 처리
        }
    }

    // access token 인지 확인 (만료된 토큰도 처리)
    public boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return "access".equals(claims.get("type"));
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서도 타입 확인 가능
            return "access".equals(e.getClaims().get("type"));
        } catch (Exception e) {
            return false;  // 예외 발생시 false 반환
        }
    }

    // refresh token 인지 확인 (만료된 토큰도 처리)
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return "refresh".equals(claims.get("type"));
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서도 타입 확인 가능
            return "refresh".equals(e.getClaims().get("type"));
        } catch (Exception e) {
            return false;  // 예외 발생시 false 반환
        }
    }

    // 토큰 만료 시간 (만료된 토큰도 처리)
    public Date getExpirationDate(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서도 만료일 반환 가능
            return e.getClaims().getExpiration();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtTokenException("유효하지 않은 토큰입니다.");
        }
    }

    // 토큰이 곧 만료되는지(30분) - 안전한 버전
    public boolean isTokenExpiringSoon(String token) {
        try {
            Date expiration = getExpirationDate(token);
            Date now = new Date();
            long timeDiff = expiration.getTime() - now.getTime();

            // 30분 이내에 만료되면 true
            return timeDiff < (30 * 60 * 1000);
        } catch (Exception e) {
            return true;  // 예외 발생시 만료 임박으로 처리
        }
    }

    // 토큰 상태를 종합적으로 검증하는 메서드 추가
    public TokenValidationResult validateTokenWithDetails(String token) {
        if (token == null || token.trim().isEmpty()) {
            return TokenValidationResult.builder()
                    .valid(false)
                    .expired(false)
                    .message("토큰이 없습니다")
                    .build();
        }

        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return TokenValidationResult.builder()
                    .valid(true)
                    .expired(false)
                    .message("유효한 토큰입니다")
                    .build();
        } catch (ExpiredJwtException e) {
            return TokenValidationResult.builder()
                    .valid(false)
                    .expired(true)
                    .message("토큰이 만료되었습니다")
                    .build();
        } catch (JwtException | IllegalArgumentException e) {
            return TokenValidationResult.builder()
                    .valid(false)
                    .expired(false)
                    .message("유효하지 않은 토큰입니다")
                    .build();
        }
    }

    // 토큰 검증 결과를 담는 내부 클래스
    @lombok.Builder
    @lombok.Getter
    public static class TokenValidationResult {
        private boolean valid;
        private boolean expired;
        private String message;
    }
}