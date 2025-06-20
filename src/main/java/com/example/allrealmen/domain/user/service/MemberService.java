package com.example.allrealmen.domain.user.service;

import com.example.allrealmen.domain.user.dto.SignUpRequest;
import com.example.allrealmen.domain.user.entity.Member;
import com.example.allrealmen.domain.user.repository.MemberRepository;
import com.example.allrealmen.domain.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements UserDetailsService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return new CustomUserDetails(member);
    }
    
    @Transactional
    public Member signUp(SignUpRequest request) {
        if (memberRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다.");
        }
        
        if (memberRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
        
        Member member = new Member();
        member.setId(request.getId());
        member.setPhoneNumber(request.getPhoneNumber());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        
        return memberRepository.save(member);
    }

    @Transactional
    public void resetPassword(String id, String newPassword) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public boolean isValidMember(String id) {
        return memberRepository.existsById(id);
    }

    public String getPhoneNumberById(String id) {
        return memberRepository.findById(id)
                .map(Member::getPhoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    public String findIdByPhoneNumber(String phoneNumber) {
        return memberRepository.findByPhoneNumber(phoneNumber)
                .map(Member::getId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 전화번호로 가입된 사용자를 찾을 수 없습니다."));
    }
} 