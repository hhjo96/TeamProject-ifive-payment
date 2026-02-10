package com.spartaifive.commercepayment.domain.user.dto;

import lombok.Getter;

@Getter
public class TokenRefreshResponse {
    private final String token;
    private final String refreshToken;

    public TokenRefreshResponse(String token, String refreshToken) {

        this.token = token;
        this.refreshToken = refreshToken;
    }
}
