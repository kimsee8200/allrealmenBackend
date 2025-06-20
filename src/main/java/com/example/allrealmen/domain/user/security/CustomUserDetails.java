package com.example.allrealmen.domain.user.security;

import com.example.allrealmen.domain.user.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private final String id;
    private final String password;
    private final String phoneNumber;
    private final Member.Role role;

    public CustomUserDetails(Member member) {
        this.id = member.getId();
        this.password = member.getPassword();
        this.phoneNumber = member.getPhoneNumber();
        this.role = member.getRole();
    }

    // JWT 토큰에서 사용자 정보를 복원할 때 사용하는 생성자
    public CustomUserDetails(String id, String password, String phoneNumber, Member.Role role) {
        this.id = id;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
} 