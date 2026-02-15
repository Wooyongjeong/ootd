package com.github.wooyong.ootd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.wooyong.ootd.domain.Comment;
import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.domain.PostLike;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.domain.WeatherType;
import com.github.wooyong.ootd.dto.CommentResponse;
import com.github.wooyong.ootd.dto.CreateCommentRequest;
import com.github.wooyong.ootd.repository.CommentRepository;
import com.github.wooyong.ootd.repository.PostLikeRepository;
import com.github.wooyong.ootd.repository.PostRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 참여 서비스(좋아요/조회/댓글) 단위 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class EngagementServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PopularRankingService popularRankingService;

    @InjectMocks
    private EngagementService engagementService;

    @Test
    void likePost_success() {
        Long postId = 10L;
        User user = User.of(1L, "tester");
        Post post = Post.of(user, WeatherType.SUNNY, "Seoul", "OOTD", null);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByUserIdAndPostId(user.getId(), postId)).thenReturn(false);

        engagementService.likePost(user, postId);

        verify(postLikeRepository).save(any());
        verify(popularRankingService).recordLike(postId, 1L);
    }

    @Test
    void likePost_duplicate_throwsConflict() {
        Long postId = 10L;
        User user = User.of(1L, "tester");
        Post post = Post.of(user, WeatherType.SUNNY, "Seoul", "OOTD", null);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByUserIdAndPostId(user.getId(), postId)).thenReturn(true);

        assertThatThrownBy(() -> engagementService.likePost(user, postId))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void unlikePost_success() {
        Long postId = 10L;
        User user = User.of(1L, "tester");
        Post post = Post.of(user, WeatherType.SUNNY, "Seoul", "OOTD", null);
        PostLike postLike = PostLike.of(user, post);

        when(postLikeRepository.findByUserIdAndPostId(user.getId(), postId)).thenReturn(Optional.of(postLike));

        engagementService.unlikePost(user, postId);

        verify(postLikeRepository).delete(postLike);
        verify(popularRankingService).recordLike(postId, -1L);
    }

    @Test
    void viewPost_success() {
        Long postId = 10L;
        User user = User.of(1L, "tester");
        Post post = Post.of(user, WeatherType.SUNNY, "Seoul", "OOTD", null);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        engagementService.viewPost(postId);

        verify(popularRankingService).recordView(postId, 1L);
    }

    @Test
    void addComment_success() {
        Long postId = 10L;
        User user = User.of(1L, "tester");
        Post post = Post.of(user, WeatherType.SUNNY, "Seoul", "OOTD", null);
        Comment comment = Comment.of(post, user, "nice");
        CreateCommentRequest request = new CreateCommentRequest("nice");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = engagementService.addComment(user, postId, request);

        verify(popularRankingService).recordComment(postId, 1L);
        assertThat(response.content()).isEqualTo("nice");
    }

    @Test
    void getComments_success() {
        Long postId = 10L;
        User user = User.of(1L, "tester");
        Post post = Post.of(user, WeatherType.SUNNY, "Seoul", "OOTD", null);
        Comment comment = Comment.of(post, user, "nice");

        when(commentRepository.findTop50ByPostIdOrderByCreatedAtDesc(postId)).thenReturn(List.of(comment));

        List<CommentResponse> result = engagementService.getComments(postId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("nice");
    }
}
