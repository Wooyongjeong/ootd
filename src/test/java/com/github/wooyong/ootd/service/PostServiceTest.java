package com.github.wooyong.ootd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.domain.WeatherType;
import com.github.wooyong.ootd.dto.CreatePostRequest;
import com.github.wooyong.ootd.dto.PageResponse;
import com.github.wooyong.ootd.dto.PopularPostResponse;
import com.github.wooyong.ootd.dto.PostResponse;
import com.github.wooyong.ootd.repository.PostRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * 게시글 서비스 단위 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PopularRankingService popularRankingService;

    @InjectMocks
    private PostService postService;

    @Test
    void createPost_success() throws Exception {
        CreatePostRequest request = new CreatePostRequest(WeatherType.SUNNY, "Seoul", "today ootd", null);
        User savedUser = User.of(1L, "user-1");
        Post savedPost = Post.of(savedUser, request.weatherType(), request.region(), request.content(), request.imageUrl());
        setId(savedPost, 100L);

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostResponse response = postService.createPost(savedUser, request);

        assertThat(response.postId()).isEqualTo(100L);
        assertThat(response.authorId()).isEqualTo(1L);
        assertThat(response.weatherType()).isEqualTo(WeatherType.SUNNY);
    }

    @Test
    void findFeed_returnsPagedResponse() {
        User user = User.of(1L, "tester");
        Post post = Post.of(user, WeatherType.CLOUDY, "Seoul", "content", null);

        when(postRepository.findFeed(WeatherType.CLOUDY, "Seoul", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 10), 1));

        PageResponse<PostResponse> response = postService.findFeed(WeatherType.CLOUDY, "Seoul", 0, 10);

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).region()).isEqualTo("Seoul");
    }

    @Test
    void findPopularPosts_keepsRankingOrder() throws Exception {
        User user = User.of(1L, "tester");
        Post first = Post.of(user, WeatherType.SUNNY, "Seoul", "a", null);
        Post second = Post.of(user, WeatherType.RAINY, "Busan", "b", null);
        setId(first, 10L);
        setId(second, 20L);

        when(popularRankingService.findTopPostScores(2))
                .thenReturn(List.of(
                        new PopularRankingService.PostScore(20L, 99.0d),
                        new PopularRankingService.PostScore(10L, 70.0d)
                ));
        when(postRepository.findAllById(List.of(20L, 10L))).thenReturn(List.of(second, first));

        List<PopularPostResponse> result = postService.findPopularPosts(2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).post().postId()).isEqualTo(20L);
        assertThat(result.get(1).post().postId()).isEqualTo(10L);
    }

    private static void setId(Post post, Long id) throws Exception {
        Field field = Post.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(post, id);
    }
}
