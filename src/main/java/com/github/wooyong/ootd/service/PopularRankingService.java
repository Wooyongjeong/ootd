package com.github.wooyong.ootd.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

/**
 * Redis 기반 실시간 인기 점수/증분 카운터 처리를 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class PopularRankingService {

    public static final String POPULAR_ZSET_KEY = "ootd:posts:popular";
    public static final String LIKE_DELTA_HASH_KEY = "ootd:posts:delta:likes";
    public static final String VIEW_DELTA_HASH_KEY = "ootd:posts:delta:views";
    public static final String COMMENT_DELTA_HASH_KEY = "ootd:posts:delta:comments";

    private final StringRedisTemplate redisTemplate;

    /**
     * 좋아요 증분을 기록하고 인기 점수(가중치 5)를 반영합니다.
     */
    public void recordLike(Long postId, long delta) {
        redisTemplate.opsForHash().increment(LIKE_DELTA_HASH_KEY, postId.toString(), delta);
        redisTemplate.opsForZSet().incrementScore(POPULAR_ZSET_KEY, postId.toString(), delta * 5.0d);
    }

    /**
     * 조회수 증분을 기록하고 인기 점수(가중치 1)를 반영합니다.
     */
    public void recordView(Long postId, long delta) {
        redisTemplate.opsForHash().increment(VIEW_DELTA_HASH_KEY, postId.toString(), delta);
        redisTemplate.opsForZSet().incrementScore(POPULAR_ZSET_KEY, postId.toString(), delta);
    }

    /**
     * 댓글 증분을 기록하고 인기 점수(가중치 3)를 반영합니다.
     */
    public void recordComment(Long postId, long delta) {
        redisTemplate.opsForHash().increment(COMMENT_DELTA_HASH_KEY, postId.toString(), delta);
        redisTemplate.opsForZSet().incrementScore(POPULAR_ZSET_KEY, postId.toString(), delta * 3.0d);
    }

    /**
     * Write-Back 대상 증분 hash를 읽어 Long 맵으로 변환합니다.
     */
    public Map<Long, Long> readDelta(String hashKey) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(hashKey);
        Map<Long, Long> result = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            result.put(Long.parseLong(entry.getKey().toString()), Long.parseLong(entry.getValue().toString()));
        }
        return result;
    }

    /**
     * 특정 게시글의 증분 값을 hash에서 제거합니다.
     */
    public void clearDelta(String hashKey, Long postId) {
        redisTemplate.opsForHash().delete(hashKey, postId.toString());
    }

    /**
     * 인기 점수 상위 게시글 목록을 점수와 함께 조회합니다.
     */
    public List<PostScore> findTopPostScores(int limit) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(POPULAR_ZSET_KEY, 0, Math.max(0, limit - 1));

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<PostScore> result = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) {
                continue;
            }
            result.add(new PostScore(Long.parseLong(tuple.getValue()), tuple.getScore()));
        }
        return result;
    }

    /**
     * 게시글 ID와 점수를 함께 담는 레코드입니다.
     */
    public record PostScore(Long postId, double score) {
    }
}
