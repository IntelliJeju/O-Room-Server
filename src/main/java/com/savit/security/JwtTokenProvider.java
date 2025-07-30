package com.savit.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

   // 토큰에서 userid 추출
    public String getUserId(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // access token 인지 확인
    public boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return "access".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    // refresh token 인지 확인
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

   // 토큰 만료 시간
    public Date getExpirationDate(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    // 토큰이 곧 만료되는지(30분)
    public boolean isTokenExpiringSoon(String token) {
        try {
            Date expiration = getExpirationDate(token);
            Date now = new Date();
            long timeDiff = expiration.getTime() - now.getTime();

            // 30분 이내에 만료되면 true
            return timeDiff < (30 * 60 * 1000);
        } catch (Exception e) {
            return true;
        }
    }

}