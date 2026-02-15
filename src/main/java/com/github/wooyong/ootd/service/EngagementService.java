package com.github.wooyong.ootd.service;

import com.github.wooyong.ootd.domain.Comment;
import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.domain.PostLike;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.dto.CommentResponse;
import com.github.wooyong.ootd.dto.CreateCommentRequest;
import com.github.wooyong.ootd.repository.CommentRepository;
import com.github.wooyong.ootd.repository.PostLikeRepository;
import com.github.wooyong.ootd.repository.PostRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
/**
 * 게시글 참여(좋아요, 조회, 댓글) 기능을 처리하는 서비스입니다.
 */
public class EngagementService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final PopularRankingService popularRankingService;

    /**
     * 좋아요를 등록하고 Redis 집계 카운터를 증가시킵니다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void likePost(User user, Long postId) {
        Post post = getPost(postId);
        if (postLikeRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already liked");
        }

        postLikeRepository.save(PostLike.of(user, post));
        popularRankingService.recordLike(postId, 1L);
    }

    /**
     * 좋아요를 취소하고 Redis 집계 카운터를 감소시킵니다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void unlikePost(User user, Long postId) {
        PostLike postLike = postLikeRepository.findByUserIdAndPostId(user.getId(), postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Like not found"));
        postLikeRepository.delete(postLike);
        popularRankingService.recordLike(postId, -1L);
    }

    /**
     * 조회 이벤트를 기록합니다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void viewPost(Long postId) {
        getPost(postId);
        popularRankingService.recordView(postId, 1L);
    }

    /**
     * 댓글을 작성하고 댓글 카운터를 증가시킵니다.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CommentResponse addComment(User user, Long postId, CreateCommentRequest request) {
        Post post = getPost(postId);
        Comment saved = commentRepository.save(Comment.of(post, user, request.content()));
        popularRankingService.recordComment(postId, 1L);
        return CommentResponse.from(saved);
    }

    /**
     * 최신 댓글 목록을 반환합니다.
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        return commentRepository.findTop50ByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    /**
     * 게시글이 없으면 404를 반환하기 위한 공통 조회 메서드입니다.
     */
    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }
}
