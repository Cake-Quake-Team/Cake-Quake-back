package com.cakequake.cakequakeback.security.domain;

import com.cakequake.cakequakeback.member.entities.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // BUYER, SELLER, ADMIN
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getUserId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 항상 유효한 계정
    }

    // 나중에 비밀번호 5회 실패 시 잠금 기능에 사용
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 되지 않음
    }

    // 회원탈퇴 처리, 이메일 인증 미완료 등에서 false 처리
    @Override
    public boolean isEnabled() {
        return true; // 활성화 된 계정
    }

    public Member getMember() {
        return member;
    }
}
