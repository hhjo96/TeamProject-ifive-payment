package com.spartaifive.commercepayment.domain.user.service;

import com.spartaifive.commercepayment.common.auth.UserDetailsImpl;
import com.spartaifive.commercepayment.common.auth.UserDetailsServiceImpl;
import com.spartaifive.commercepayment.common.security.JwtTokenProvider;
import com.spartaifive.commercepayment.domain.token.entity.Token;
import com.spartaifive.commercepayment.domain.token.repository.TokenRepository;
import com.spartaifive.commercepayment.domain.user.dto.CreateUserRequest;
import com.spartaifive.commercepayment.domain.user.dto.CreateUserResponse;
import com.spartaifive.commercepayment.domain.user.dto.LoginRequest;
import com.spartaifive.commercepayment.domain.user.dto.LoginResponse;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;

    @Transactional
    public CreateUserResponse save(CreateUserRequest request){

        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("이메일이 이미 존재");
        }
        if (userRepository.existsByPhone(request.getPhone())){
            throw new RuntimeException("전화번호가 이미 존재");
        }
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone()
        );
        User userSaved = userRepository.save(user);
        return new CreateUserResponse(
                userSaved.getId(),
                userSaved.getName(),
                userSaved.getEmail(),
                userSaved.getPhone()
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserDetails userDetails = userDetailsServiceImpl.loadUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 일치하지 않음");
        }
        UserDetailsImpl impl = (UserDetailsImpl) userDetails;
        User user = impl.getUser();

        //중복 로그인 처리
        tokenRepository.logoutAllByUserId(user.getId());

        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        //리프레쉬 토큰 DB저장
        Token token = Token.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiredAt(LocalDateTime.now().plus(jwtTokenProvider.getRefreshTokenExpiration()))
                .build();

        tokenRepository.save(token);
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                accessToken
        );
    }
}
