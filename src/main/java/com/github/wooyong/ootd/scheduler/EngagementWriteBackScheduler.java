package com.github.wooyong.ootd.scheduler;

import com.github.wooyong.ootd.domain.Post;
import com.github.wooyong.ootd.repository.PostRepository;
import com.github.wooyong.ootd.service.PopularRankingService;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * Redis에 누적된 참여 지표를 주기적으로 DB에 반영하는 Write-Back 스케줄러입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EngagementWriteBackScheduler {

    private final PostRepository postRepository;
    private final PopularRankingService popularRankingService;

    /**
     * 좋아요/조회/댓글 증분을 읽어 게시글 카운터에 반영한 뒤 Redis 증분값을 비웁니다.
     */
    @Scheduled(fixedDelayString = "${app.write-back-interval-ms:300000}")
    @Transactional
    public void flushEngagementCounters() {
        Map<Long, Long> likeDeltaMap = popularRankingService.readDelta(PopularRankingService.LIKE_DELTA_HASH_KEY);
        Map<Long, Long> viewDeltaMap = popularRankingService.readDelta(PopularRankingService.VIEW_DELTA_HASH_KEY);
        Map<Long, Long> commentDeltaMap = popularRankingService.readDelta(PopularRankingService.COMMENT_DELTA_HASH_KEY);

        Set<Long> postIds = new HashSet<>();
        postIds.addAll(likeDeltaMap.keySet());
        postIds.addAll(viewDeltaMap.keySet());
        postIds.addAll(commentDeltaMap.keySet());

        for (Long postId : postIds) {
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                clearDelta(postId);
                continue;
            }

            long likeDelta = likeDeltaMap.getOrDefault(postId, 0L);
            long viewDelta = viewDeltaMap.getOrDefault(postId, 0L);
            long commentDelta = commentDeltaMap.getOrDefault(postId, 0L);

            post.applyEngagementDelta(likeDelta, viewDelta, commentDelta);
            postRepository.save(post);
            clearDelta(postId);
        }

        if (!postIds.isEmpty()) {
            log.info("Write-back completed. posts={}", postIds.size());
        }
    }

    /**
     * 단일 게시글의 모든 증분 hash 키를 정리합니다.
     */
    private void clearDelta(Long postId) {
        popularRankingService.clearDelta(PopularRankingService.LIKE_DELTA_HASH_KEY, postId);
        popularRankingService.clearDelta(PopularRankingService.VIEW_DELTA_HASH_KEY, postId);
        popularRankingService.clearDelta(PopularRankingService.COMMENT_DELTA_HASH_KEY, postId);
    }
}
