package com.example.allrealmen.domain.user.controller;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.user.dto.ResetPasswordRequest;
import com.example.allrealmen.domain.user.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordController {

    private final MemberService memberService;

    @GetMapping("/reset-pass")
    public ResponseEntity<ApiResponse<Boolean>> checkValidMember(@RequestParam String id) {
        boolean isValid = memberService.isValidMember(id);
        if (isValid) {
            return ResponseEntity.ok(new ApiResponse<>("success", true, null));
        } else {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("error", false, null));
        }
    }

    @PutMapping("/reset-pass")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            memberService.resetPassword(request.getId(), request.getPassword());
            return ResponseEntity.ok(new ApiResponse<>("success", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("error", null, e.getMessage()));
        }
    }
} 