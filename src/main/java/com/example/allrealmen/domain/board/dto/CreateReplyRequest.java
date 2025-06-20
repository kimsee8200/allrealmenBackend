package com.example.allrealmen.domain.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateReplyRequest {
    @NotBlank(message = "댓글 내용은 필수 입력값입니다.")
    private String comment;
} 