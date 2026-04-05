package org.iimsa.hub_service.hubroute.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.cache.RouteSnapshotCache;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HubRouteCacheRepositoryImpl implements HubRouteCacheRepository {

    private final StringRedisTemplate redisTemplate;

    @Qualifier("redisObjectMapper")
    private final ObjectMapper objectMapper;

    // TTL 설정
    private static final Duration LIVE_TTL = Duration.ofMinutes(20);
    private static final Duration SNAPSHOT_TTL = Duration.ofDays(2);
    private static final Duration LATEST_SNAPSHOT_ID_TTL = Duration.ofDays(2);

    // 키 포맷
    private static final String LIVE_KEY = "hub:route:live:%s:%s";
    private static final String SNAPSHOT_KEY = "hub:route:snapshot:%s:%s:%s";
    private static final String LATEST_SNAPSHOT_ID_KEY = "hub:route:snapshot:latest";

    // ── Live 캐시 ──────────────────────────────────

    @Override
    public Optional<LiveRouteCache> getLive(UUID fromHubId, UUID toHubId) {
        String key = liveKey(fromHubId, toHubId);
        return get(key, LiveRouteCache.class);
    }

    @Override
    public void setLive(UUID fromHubId, UUID toHubId, LiveRouteCache cache) {
        String key = liveKey(fromHubId, toHubId);
        set(key, cache, LIVE_TTL);
    }

    // ── 배차 스냅샷 ────────────────────────────────

    @Override
    public Optional<RouteSnapshotCache> getSnapshot(String snapshotId, UUID fromHubId, UUID toHubId) {
        String key = snapshotKey(snapshotId, fromHubId, toHubId);
        return get(key, RouteSnapshotCache.class);
    }

    @Override
    public void setSnapshot(String snapshotId, UUID fromHubId, UUID toHubId, RouteSnapshotCache cache) {
        String key = snapshotKey(snapshotId, fromHubId, toHubId);
        set(key, cache, SNAPSHOT_TTL);
    }

    // ── 최신 스냅샷 ID ─────────────────────────────

    @Override
    public Optional<String> getLatestSnapshotId() {
        return Optional.ofNullable(redisTemplate.opsForValue().get(LATEST_SNAPSHOT_ID_KEY));
    }

    @Override
    public void setLatestSnapshotId(String snapshotId) {
        redisTemplate.opsForValue().set(LATEST_SNAPSHOT_ID_KEY, snapshotId, LATEST_SNAPSHOT_ID_TTL);
    }

    // ── 내부 유틸 ──────────────────────────────────

    private String liveKey(UUID fromHubId, UUID toHubId) {
        return String.format(LIVE_KEY, fromHubId, toHubId);
    }

    private String snapshotKey(String snapshotId, UUID fromHubId, UUID toHubId) {
        return String.format(SNAPSHOT_KEY, snapshotId, fromHubId, toHubId);
    }

    private <T> Optional<T> get(String key, Class<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception e) {
            log.warn("Redis 캐시 읽기 실패 - key={}, error={}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private void set(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            log.warn("Redis 캐시 쓰기 실패 - key={}, error={}", key, e.getMessage());
        }
    }
}
