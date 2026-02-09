package com.spartaifive.commercepayment.domain.user.controller;

import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.user.dto.CreateUserRequest;
import com.spartaifive.commercepayment.domain.user.dto.CreateUserResponse;
import com.spartaifive.commercepayment.domain.user.dto.LoginRequest;
import com.spartaifive.commercepayment.domain.user.dto.LoginResponse;
import com.spartaifive.commercepayment.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<DataResponse<CreateUserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        CreateUserResponse res = userService.save(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DataResponse.success("SUCCESS", res));
    }
    @PostMapping("/login")
    public ResponseEntity<DataResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse res = userService.login(request);
        return  ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success("SUCCESS", res));
    }



}
