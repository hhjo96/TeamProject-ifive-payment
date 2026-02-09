package com.spartaifive.commercepayment.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 * 개선할 부분: Refresh Token, Token Expiry 관리, Claims 커스터마이징 등
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    // Access Token 만료 시간: 30분
    private static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000L;

    // Refresh Token 만료 시간: 7일
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    public JwtTokenProvider(
        @Value("${jwt.secret:commercehub-secret-key-for-demo-please-change-this-in-production-environment}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    //AccessToken: 만료 시간 30분
    public String generateAccessToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("type", "access")//토큰 타입 구분
                .issuedAt(now)//발급 시간
                .expiration(expiryDate)//만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     * Access Token 재발급용
     * 만료 시간: 7일
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 만료 시간 반환
     * DB 저장용
     */
    public long getRefreshTokenExpiration() {
        return REFRESH_TOKEN_EXPIRATION;
    }

    /**
     * 토큰에서 사용자 ID 추출

     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)//검증키 설정
                .build()
                .parseSignedClaims(token)//검증 수행(서명)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰에서 이메일 추출

     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    //토큰 예외 처리
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("토큰이 만료되었습니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 토큰입니다: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("토큰 서명이 유효하지 않습니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("토큰이 비어있습니다: {}", e.getMessage());
        }
        return false;
    }
}
