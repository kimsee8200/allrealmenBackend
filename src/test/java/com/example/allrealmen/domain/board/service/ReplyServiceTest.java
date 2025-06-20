package com.example.allrealmen.domain.board.service;

import com.example.allrealmen.common.util.SecurityUtil;
import com.example.allrealmen.domain.board.dto.CreateReplyRequest;
import com.example.allrealmen.domain.board.dto.ReplyResponse;
import com.example.allrealmen.domain.board.entity.Post;
import com.example.allrealmen.domain.board.entity.Reply;
import com.example.allrealmen.domain.board.repository.PostRepository;
import com.example.allrealmen.domain.board.repository.ReplyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplyServiceTest {

    @InjectMocks
    private ReplyService replyService;

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private PostRepository postRepository;

    private Post post;
    private Reply reply;
    private CreateReplyRequest createReplyRequest;
    private String currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = "testUser";
        post = Post.builder()
            .id("1")
            .title("Test Title")
            .content("Test Content")
            .writerId(currentUserId)
            .build();

        reply = Reply.builder()
            .id("1")
            .comment("Test Comment")
            .writerId(currentUserId)
            .postId(post.getId())
            .build();

        createReplyRequest = new CreateReplyRequest();
        createReplyRequest.setComment("New Comment");
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getRepliesSuccess() {
        // given
        when(replyRepository.findAllByPostIdOrderByCreateAtAsc(any())).thenReturn(List.of(reply));

        // when
        List<ReplyResponse> result = replyService.getReplies(post.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getComment()).isEqualTo(reply.getComment());
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void createReplySuccess() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
            when(postRepository.findById(any())).thenReturn(Optional.of(post));
            when(replyRepository.save(any())).thenReturn(reply);

            // when
            ReplyResponse result = replyService.createReply(post.getId(), createReplyRequest);

            // then
            assertThat(result.getComment()).isEqualTo(reply.getComment());
            verify(replyRepository).save(any(Reply.class));
        }
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 작성 시도")
    void createReplyToNonExistingPost() {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> replyService.createReply("nonExistingId", createReplyRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("게시글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateReplySuccess() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
            when(replyRepository.findById(any())).thenReturn(Optional.of(reply));
            when(replyRepository.save(any())).thenReturn(reply);

            // when
            ReplyResponse result = replyService.updateReply(reply.getId(), "Updated Comment");

            // then
            assertThat(result.getComment()).isEqualTo(reply.getComment());
            verify(replyRepository).save(any(Reply.class));
        }
    }

    @Test
    @DisplayName("권한이 없는 댓글 수정 시도")
    void updateReplyWithoutPermission() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn("otherUser");
            when(replyRepository.findById(any())).thenReturn(Optional.of(reply));

            // when & then
            assertThatThrownBy(() -> replyService.updateReply(reply.getId(), "Updated Comment"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("댓글을 수정할 권한이 없습니다.");
        }
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteReplySuccess() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(currentUserId);
            when(replyRepository.findById(any())).thenReturn(Optional.of(reply));
            when(postRepository.findById(any())).thenReturn(Optional.of(post));
            doNothing().when(replyRepository).delete(any());

            // when
            replyService.deleteReply(reply.getId());

            // then
            verify(replyRepository).delete(reply);
        }
    }

    @Test
    @DisplayName("권한이 없는 댓글 삭제 시도")
    void deleteReplyWithoutPermission() {
        // given
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn("otherUser");
            when(replyRepository.findById(any())).thenReturn(Optional.of(reply));

            // when & then
            assertThatThrownBy(() -> replyService.deleteReply(reply.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("댓글을 삭제할 권한이 없습니다.");
        }
    }
} 