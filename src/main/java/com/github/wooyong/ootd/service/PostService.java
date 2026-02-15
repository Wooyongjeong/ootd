package com.github.wooyong.ootd.service;

import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.domain.WeatherType;
import com.github.wooyong.ootd.dto.CreatePostRequest;
import com.github.wooyong.ootd.dto.PageResponse;
import com.github.wooyong.ootd.dto.PopularPostResponse;
import com.github.wooyong.ootd.dto.PostResponse;
import com.github.wooyong.ootd.repository.PostRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 게시물 생성/조회 유스케이스를 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PopularRankingService popularRankingService;

    /**
     * 인증된 작성자로 게시글을 생성합니다.
     */
    @Transactional
    public PostResponse createPost(User author, CreatePostRequest request) {
        Post post = postRepository.save(Post.of(
                author,
                request.weatherType(),
                request.region(),
                request.content(),
                request.imageUrl()
        ));
        return PostResponse.from(post);
    }

    /**
     * 조건 기반 피드를 페이지 형태로 조회합니다.
     */
    @Transactional(readOnly = true)
    public PageResponse<PostResponse> findFeed(WeatherType weatherType, String region, int page, int size) {
        Page<PostResponse> result = postRepository
                .findFeed(weatherType, region, PageRequest.of(page, size))
                .map(PostResponse::from);
        return PageResponse.from(result);
    }

    /**
     * Redis 실시간 점수를 기반으로 인기 게시글을 조회합니다.
     * 점수 순서를 보존하기 위해 score 목록 순서대로 매핑합니다.
     */
    @Transactional(readOnly = true)
    public List<PopularPostResponse> findPopularPosts(int limit) {
        List<PopularRankingService.PostScore> topScores = popularRankingService.findTopPostScores(limit);
        List<Long> ids = topScores.stream().map(PopularRankingService.PostScore::postId).toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Long, Post> postMap = new LinkedHashMap<>();
        postRepository.findAllById(ids).forEach(post -> postMap.put(post.getId(), post));

        return topScores.stream()
                .filter(score -> postMap.containsKey(score.postId()))
                .map(score -> new PopularPostResponse(PostResponse.from(postMap.get(score.postId())), score.score()))
                .toList();
    }
}
