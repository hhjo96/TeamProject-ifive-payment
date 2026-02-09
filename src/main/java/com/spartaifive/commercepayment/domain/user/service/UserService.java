package com.spartaifive.commercepayment.domain.user.service;

import com.spartaifive.commercepayment.domain.user.dto.CreateUserRequest;
import com.spartaifive.commercepayment.domain.user.dto.CreateUserResponse;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
