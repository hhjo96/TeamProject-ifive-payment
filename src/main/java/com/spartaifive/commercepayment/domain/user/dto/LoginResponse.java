package com.spartaifive.commercepayment.domain.user.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final String token;
    private final String refreshToken;

    public LoginResponse(Long id, String name, String email, String token,  String refreshToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
