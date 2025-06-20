package com.example.allrealmen.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtil {

    private SecurityUtil() {
        // private constructor to prevent instantiation
    }

    /**
     * 현재 로그인한 사용자의 이메일을 반환합니다.
     * OAuth2 인증 및 일반 인증 모두 지원합니다.
     *
     * @return 현재 인증된 사용자의 이메일 (Optional)
     */
    public static Optional<String> getCurrentUserEmail() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // OAuth2 인증인 경우
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return Optional.ofNullable(oauth2User.getAttribute("email"));
        }

        // 일반 인증인 경우
        return Optional.ofNullable(authentication.getName());
    }

    /**
     * 현재 로그인한 사용자의 ID를 반환합니다.
     *
     * @return 현재 인증된 사용자의 ID (Optional)
     */
    public static String getCurrentUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authentication found");
        }

        return authentication.getName();
    }

    /**
     * 현재 사용자가 인증되었는지 확인합니다.
     *
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 현재 사용자가 특정 권한을 가지고 있는지 확인합니다.
     *
     * @param authority 확인할 권한
     * @return 권한 보유 여부
     */
    public static boolean hasAuthority(String authority) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    /**
     * 현재 OAuth2 사용자의 속성값을 반환합니다.
     *
     * @param attributeName 속성 이름
     * @return 속성값 (Optional)
     */
    public static Optional<Object> getOAuth2Attribute(String attributeName) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof OAuth2User)) {
            return Optional.empty();
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        return Optional.ofNullable(oauth2User.getAttribute(attributeName));
    }
}
