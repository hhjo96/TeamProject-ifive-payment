package com.spartaifive.commercepayment.common.auth;

import com.spartaifive.commercepayment.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final User user;

    /**
     * 사용자 ID 반환
     * Controller, Service에서 활용
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 사용자 이메일 반환
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * 사용자 이름 반환
     */
    public String getName() {
        return user.getName();
    }

    // 현재는 권한 없음 (필요 시 추가)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // 이메일이 아이디로 사용
    }

    //계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //계정 잠금 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //비밀번호 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //계정 비활성화 여부
    @Override
    public boolean isEnabled() {
        return true;
    }
}
