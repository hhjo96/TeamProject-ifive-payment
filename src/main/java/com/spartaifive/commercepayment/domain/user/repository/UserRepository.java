package com.spartaifive.commercepayment.domain.user.repository;

import com.spartaifive.commercepayment.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail( String email);
    boolean existsByPhone(String phone);
}
