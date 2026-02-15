package com.github.wooyong.ootd.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.domain.User;
import com.github.wooyong.ootd.domain.WeatherType;
import com.github.wooyong.ootd.repository.PostRepository;
import com.github.wooyong.ootd.service.PopularRankingService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Write-Back 스케줄러 단위 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class EngagementWriteBackSchedulerTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PopularRankingService popularRankingService;

    @InjectMocks
    private EngagementWriteBackScheduler scheduler;

    @Test
    void flushEngagementCounters_appliesDeltasAndClearsRedis() {
        Long postId = 100L;
        User user = User.of(1L, "tester");
        Post post = Post.of(user, WeatherType.CLOUDY, "Seoul", "test", null);

        when(popularRankingService.readDelta(PopularRankingService.LIKE_DELTA_HASH_KEY))
                .thenReturn(Map.of(postId, 2L));
        when(popularRankingService.readDelta(PopularRankingService.VIEW_DELTA_HASH_KEY))
                .thenReturn(Map.of(postId, 5L));
        when(popularRankingService.readDelta(PopularRankingService.COMMENT_DELTA_HASH_KEY))
                .thenReturn(Map.of(postId, 1L));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        scheduler.flushEngagementCounters();

        assertThat(post.getLikeCount()).isEqualTo(2L);
        assertThat(post.getViewCount()).isEqualTo(5L);
        assertThat(post.getCommentCount()).isEqualTo(1L);
        verify(postRepository).save(post);
        verify(popularRankingService).clearDelta(PopularRankingService.LIKE_DELTA_HASH_KEY, postId);
        verify(popularRankingService).clearDelta(PopularRankingService.VIEW_DELTA_HASH_KEY, postId);
        verify(popularRankingService).clearDelta(PopularRankingService.COMMENT_DELTA_HASH_KEY, postId);
    }
}
