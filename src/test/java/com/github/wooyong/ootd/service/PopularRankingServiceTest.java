package com.github.wooyong.ootd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;

/**
 * Redis 인기 점수 서비스 단위 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class PopularRankingServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private PopularRankingService popularRankingService;

    @Test
    void recordLike_updatesHashAndZSet() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        popularRankingService.recordLike(10L, 1L);

        verify(hashOperations).increment(PopularRankingService.LIKE_DELTA_HASH_KEY, "10", 1L);
        verify(zSetOperations).incrementScore(PopularRankingService.POPULAR_ZSET_KEY, "10", 5.0d);
    }

    @Test
    void readDelta_convertsRedisEntries() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(PopularRankingService.VIEW_DELTA_HASH_KEY))
                .thenReturn(Map.of("1", "3", "2", "5"));

        Map<Long, Long> result = popularRankingService.readDelta(PopularRankingService.VIEW_DELTA_HASH_KEY);

        assertThat(result).containsEntry(1L, 3L).containsEntry(2L, 5L);
    }

    @Test
    void findTopPostScores_returnsOrderedScores() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("7", 42.0d));
        tuples.add(new DefaultTypedTuple<>("3", 11.0d));
        when(zSetOperations.reverseRangeWithScores(PopularRankingService.POPULAR_ZSET_KEY, 0, 1))
                .thenReturn(tuples);

        var result = popularRankingService.findTopPostScores(2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).postId()).isEqualTo(7L);
        assertThat(result.get(0).score()).isEqualTo(42.0d);
        assertThat(result.get(1).postId()).isEqualTo(3L);
    }
}
