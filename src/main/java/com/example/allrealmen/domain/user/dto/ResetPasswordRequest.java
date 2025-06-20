package com.example.allrealmen.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "아이디는 필수 입력값입니다.")
    private String id;
    
    @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
    private String password;
} 