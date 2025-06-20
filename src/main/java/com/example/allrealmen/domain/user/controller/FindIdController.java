package com.example.allrealmen.domain.user.controller;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.user.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class FindIdController {

    private final MemberService memberService;

    @GetMapping("/find-id")
    public ResponseEntity<ApiResponse<String>> findId(@RequestParam String phone) {
        try {
            String userId = memberService.findIdByPhoneNumber(phone);
            return ResponseEntity.ok(new ApiResponse<>("success", userId, null));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("error", null, e.getMessage()));
        }
    }
} 