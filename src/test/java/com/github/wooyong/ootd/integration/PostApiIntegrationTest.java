package com.github.wooyong.ootd.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.domain.WeatherType;
import com.github.wooyong.ootd.auth.JwtTokenProvider;
import com.github.wooyong.ootd.repository.PostRepository;
import com.github.wooyong.ootd.repository.UserRepository;
import com.github.wooyong.ootd.service.PopularRankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 게시글 API 통합 테스트입니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostApiIntegrationTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        PopularRankingService popularRankingService() {
            return Mockito.mock(PopularRankingService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PopularRankingService popularRankingService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setup() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createAndFeed() throws Exception {
        User user = userRepository.save(User.of(1L, "tester"));
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String request = """
                {
                  "weatherType":"SUNNY",
                  "region":"Seoul",
                  "content":"spring ootd",
                  "imageUrl":"https://img.test/1.jpg"
                }
                """;

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON-201"))
                .andExpect(jsonPath("$.data.authorId").value(1))
                .andExpect(jsonPath("$.data.weatherType").value("SUNNY"));

        mockMvc.perform(get("/api/posts/feed")
                        .param("weatherType", "SUNNY")
                        .param("region", "Seoul")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].region").value("Seoul"));
    }

    @Test
    void likeUnlikeAndCommentFlow() throws Exception {
        User user = userRepository.save(User.of(1L, "tester"));
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        Post post = postRepository.save(Post.of(user, WeatherType.CLOUDY, "Busan", "ootd", null));

        doNothing().when(popularRankingService).recordLike(post.getId(), 1L);
        doNothing().when(popularRankingService).recordLike(post.getId(), -1L);
        doNothing().when(popularRankingService).recordComment(post.getId(), 1L);

        mockMvc.perform(post("/api/posts/{postId}/likes", post.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON-200"));

        mockMvc.perform(delete("/api/posts/{postId}/likes", post.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON-200"));

        mockMvc.perform(post("/api/posts/{postId}/comments", post.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"nice look\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON-201"))
                .andExpect(jsonPath("$.data.content").value("nice look"));

        mockMvc.perform(get("/api/posts/{postId}/comments", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].content").value("nice look"));
    }

    @Test
    void popularEndpoint() throws Exception {
        User user = userRepository.save(User.of(1L, "tester"));
        Post post = postRepository.save(Post.of(user, WeatherType.WINDY, "Incheon", "windy", null));
        when(popularRankingService.findTopPostScores(10))
                .thenReturn(java.util.List.of(new PopularRankingService.PostScore(post.getId(), 123.0)));

        mockMvc.perform(get("/api/posts/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].post.postId").value(post.getId()))
                .andExpect(jsonPath("$.data[0].score").value(123.0));
    }

    @Test
    void createPost_withoutToken_unauthorized() throws Exception {
        String request = """
                {
                  "weatherType":"SUNNY",
                  "region":"Seoul",
                  "content":"spring ootd"
                }
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON-401"));
    }

    @Test
    void like_duplicate_returnsConflictResponse() throws Exception {
        User user = userRepository.save(User.of(1L, "tester"));
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        Post post = postRepository.save(Post.of(user, WeatherType.SUNNY, "Seoul", "ootd", null));
        doNothing().when(popularRankingService).recordLike(post.getId(), 1L);

        mockMvc.perform(post("/api/posts/{postId}/likes", post.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/posts/{postId}/likes", post.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON-409"));
    }

    @Test
    void createPost_validationError_returnsCommonErrorResponse() throws Exception {
        User user = userRepository.save(User.of(1L, "tester"));
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "region": "",
                                  "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON-422"))
                .andExpect(jsonPath("$.data").isMap());
    }
}
