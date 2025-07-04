package com.example.allrealmen.domain.user.security;

import com.example.allrealmen.domain.user.entity.Member;
import com.example.allrealmen.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        Map<String, Object> attributes = oauth2User.getAttributes();
        OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, attributes);
        
        Member member = memberRepository.findById(userInfo.getId())
                .orElseGet(() -> createNewMember(userInfo));

        return new CustomOAuth2User(member, attributes, userInfo.getProvider());
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("naver")) {
            return new NaverOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            return new KakaoOAuth2UserInfo(attributes);
        }
        throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
    }

    private Member createNewMember(OAuth2UserInfo userInfo) {
        Member member = new Member();
        member.setId(userInfo.getName()); // 임시 ID 생성
        
        // 네이버 사용자인 경우에만 전화번호 저장
        if (userInfo instanceof NaverOAuth2UserInfo) {
            NaverOAuth2UserInfo naverUserInfo = (NaverOAuth2UserInfo) userInfo;
            member.setPhoneNumber(naverUserInfo.getMobile());
        } else {
            // 다른 OAuth 제공자의 경우 전화번호를 저장하지 않음
            member.setPhoneNumber(null);
        }
        
        //member.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 임시 비밀번호
        return memberRepository.save(member);
    }
} 