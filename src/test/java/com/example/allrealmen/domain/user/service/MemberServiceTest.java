package com.example.allrealmen.domain.user.service;

import com.example.allrealmen.domain.user.dto.SignUpRequest;
import com.example.allrealmen.domain.user.entity.Member;
import com.example.allrealmen.domain.user.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SmsService smsService;

    private SignUpRequest signUpRequest;
    private Member member;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest();
        signUpRequest.setId("testUser");
        signUpRequest.setPassword("password123");
        signUpRequest.setPhoneNumber("01012345678");

        member = new Member();
        member.setId(signUpRequest.getId());
        member.setPhoneNumber(signUpRequest.getPhoneNumber());
        member.setPassword("encodedPassword");
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUpSuccess() {
        // given
        when(memberRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(memberRepository.existsById(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(memberRepository.save(any())).thenReturn(member);

        // when
        Member savedMember = memberService.signUp(signUpRequest);

        // then
        assertThat(savedMember.getId()).isEqualTo(signUpRequest.getId());
        assertThat(savedMember.getPhoneNumber()).isEqualTo(signUpRequest.getPhoneNumber());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("이미 존재하는 전화번호로 회원가입 시도")
    void signUpWithExistingPhoneNumber() {
        // given
        when(memberRepository.existsByPhoneNumber(any())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.signUp(signUpRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 가입된 전화번호입니다.");
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 회원가입 시도")
    void signUpWithExistingId() {
        // given
        when(memberRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(memberRepository.existsById(any())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.signUp(signUpRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 사용중인 아이디입니다.");
    }

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void resetPasswordSuccess() {
        // given
        String newPassword = "newPassword123";
        when(memberRepository.findById(any())).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(any())).thenReturn("encodedNewPassword");

        // when
        memberService.resetPassword(member.getId(), newPassword);

        // then
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 비밀번호 재설정 시도")
    void resetPasswordWithNonExistingUser() {
        // given
        when(memberRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.resetPassword("nonExistingUser", "newPassword"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("회원 존재 여부 확인")
    void isValidMember() {
        // given
        when(memberRepository.existsById(any())).thenReturn(true);

        // when
        boolean isValid = memberService.isValidMember("testUser");

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("전화번호로 아이디 찾기 성공")
    void findIdByPhoneNumberSuccess() {
        // given
        when(memberRepository.findByPhoneNumber(any())).thenReturn(Optional.of(member));

        // when
        String foundId = memberService.findIdByPhoneNumber("01012345678");

        // then
        assertThat(foundId).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("존재하지 않는 전화번호로 아이디 찾기 시도")
    void findIdByNonExistingPhoneNumber() {
        // given
        when(memberRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.findIdByPhoneNumber("01012345678"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("해당 전화번호로 가입된 사용자를 찾을 수 없습니다.");
    }
} 