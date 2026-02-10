package com.spartaifive.commercepayment.domain.user.dto;

import lombok.Getter;

@Getter
public class MeResponse {
    private final String customerUid;
    private final String email;
    private final String name;
    private final String phone;
    private final Long pointBalance;

    public MeResponse(String customerUid, String email, String name, String phone, Long pointBalance) {
        this.customerUid = customerUid;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.pointBalance = pointBalance;
    }

}
