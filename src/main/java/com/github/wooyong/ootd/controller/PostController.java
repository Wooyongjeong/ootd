package com.github.wooyong.ootd.controller;

import com.github.wooyong.ootd.auth.LoginUser;
import com.github.wooyong.ootd.common.ApiResponse;
import com.github.wooyong.ootd.common.ResponseCode;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.domain.WeatherType;
import com.github.wooyong.ootd.dto.CommentResponse;
import com.github.wooyong.ootd.dto.CreateCommentRequest;
import com.github.wooyong.ootd.dto.CreatePostRequest;
import com.github.wooyong.ootd.dto.PageResponse;
import com.github.wooyong.ootd.dto.PopularPostResponse;
import com.github.wooyong.ootd.dto.PostResponse;
import com.github.wooyong.ootd.service.EngagementService;
import com.github.wooyong.ootd.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Post and engagement API controller.
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final EngagementService engagementService;

    /**
     * Create a post.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @LoginUser User user,
            @Valid @RequestBody CreatePostRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResponseCode.CREATED, postService.createPost(user, request)));
    }

    /**
     * Get feed by weather and region filter.
     */
    @GetMapping("/feed")
    public ApiResponse<PageResponse<PostResponse>> feed(
            @RequestParam(required = false) WeatherType weatherType,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(ResponseCode.OK, postService.findFeed(weatherType, region, page, size));
    }

    /**
     * Get popular posts.
     */
    @GetMapping("/popular")
    public ApiResponse<List<PopularPostResponse>> popular(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(ResponseCode.OK, postService.findPopularPosts(limit));
    }

    /**
     * Like a post.
     */
    @PostMapping("/{postId}/likes")
    public ApiResponse<Void> like(@LoginUser User user, @PathVariable Long postId) {
        engagementService.likePost(user, postId);
        return ApiResponse.success(ResponseCode.OK);
    }

    /**
     * Unlike a post.
     */
    @DeleteMapping("/{postId}/likes")
    public ApiResponse<Void> unlike(@LoginUser User user, @PathVariable Long postId) {
        engagementService.unlikePost(user, postId);
        return ApiResponse.success(ResponseCode.OK);
    }

    /**
     * Record a post view.
     */
    @PostMapping("/{postId}/views")
    public ApiResponse<Void> view(@PathVariable Long postId) {
        engagementService.viewPost(postId);
        return ApiResponse.success(ResponseCode.OK);
    }

    /**
     * Create a comment.
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> comment(
            @LoginUser User user,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResponseCode.CREATED, engagementService.addComment(user, postId, request)));
    }

    /**
     * Get recent comments.
     */
    @GetMapping("/{postId}/comments")
    public ApiResponse<List<CommentResponse>> comments(@PathVariable Long postId) {
        return ApiResponse.success(ResponseCode.OK, engagementService.getComments(postId));
    }
}

