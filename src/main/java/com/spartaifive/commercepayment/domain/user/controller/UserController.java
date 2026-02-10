package com.spartaifive.commercepayment.domain.user.controller;

import com.spartaifive.commercepayment.common.auth.AuthUtil;
import com.spartaifive.commercepayment.common.auth.UserDetailsImpl;
import com.spartaifive.commercepayment.common.auth.UserDetailsServiceImpl;
import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.common.response.MessageResponse;
import com.spartaifive.commercepayment.domain.user.dto.*;
import com.spartaifive.commercepayment.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @PostMapping("/signup")
    public ResponseEntity<DataResponse<CreateUserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        CreateUserResponse res = userService.save(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DataResponse.success("SUCCESS", res));
    }
    @PostMapping("/login")
    public ResponseEntity<DataResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,  HttpServletResponse response) {
        LoginResponse res = userService.login(request);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", res.getRefreshToken())
                .httpOnly(true)
                .secure(false)          // 로컬은 false, https 배포면 true
                .sameSite("Lax")        // 같은 도메인이면 Lax로 충분
                .path("/api/auth")      // 범위로 제한 (원하면 "/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return  ResponseEntity
                .status(HttpStatus.OK)
                .header("Authorization", "Bearer " + res.getToken())
                .body(DataResponse.success("SUCCESS", res));
    }
    @GetMapping("/me")
    public ResponseEntity<DataResponse<MeResponse>> me(
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl
            ) {
        Long userId = AuthUtil.getCurrentUserId();
        MeResponse res = userService.getMe(userId);

        return ResponseEntity.ok(DataResponse.success("SUCCESS", res));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse<Void>> logout() {

        // SecurityContext에서 현재 사용자 ID 추출
        Long currentUserId = AuthUtil.getCurrentUserId();

        // 로그아웃 처리
        userService.logout(currentUserId);

        return ResponseEntity.ok(MessageResponse.success( "SUCCESS","로그아웃되었습니다."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<DataResponse<TokenRefreshResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
//        if (refreshToken == null) {
//            // 401로 통일 (에러코드 스타일에 맞춰 커스텀 예외로 바꿔도 됨)
//            return ResponseEntity.status(401)
//                    .body(DataResponse.fail("UNAUTHORIZED", "Refresh Token이 없습니다."));
//        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("REFRESH_TOKEN_MISSING");
        }

        TokenRefreshResponse res = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(DataResponse.success( "SUCCESS", res));
    }



}
