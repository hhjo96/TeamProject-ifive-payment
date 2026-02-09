package com.spartaifive.commercepayment.domain.token.repository;

import com.spartaifive.commercepayment.domain.token.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByRefreshToken(String refreshToken);

    //중복 로그인 방지
    @Modifying
    @Query("UPDATE Token t SET t.isLogout = true WHERE t.user.id = :userId")
    void logoutAllByUserId(@Param("userId") Long userId);
}
