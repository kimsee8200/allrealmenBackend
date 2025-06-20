package com.example.allrealmen.domain.user.controller;
import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.user.security.CustomUserDetails;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionInfoController {

    @GetMapping("/session/check")
    public ResponseEntity<ApiResponse<CustomUserDetails>> checkSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentMember = (CustomUserDetails) authentication.getPrincipal();
        return new ResponseEntity<>(
            new ApiResponse<>("200 OK", currentMember, "정상적으로 처리되었습니다."),
            HttpStatusCode.valueOf(200)
        );
    }
}
