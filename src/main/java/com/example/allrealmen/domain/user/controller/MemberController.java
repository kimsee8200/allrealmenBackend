package com.example.allrealmen.domain.user.controller;

import com.example.allrealmen.common.util.SecurityUtil;
import com.example.allrealmen.domain.user.dto.MemberUpdateDto;
import com.example.allrealmen.domain.user.entity.Member;
import com.example.allrealmen.domain.user.service.MemberService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.SecurityConfig;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PutMapping("/me")
    public ResponseEntity<String> updateMember(@RequestBody MemberUpdateDto updateDto) {
        String currentMember = SecurityUtil.getCurrentUserId();
        memberService.updateMember(currentMember, updateDto);
        
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }
} 