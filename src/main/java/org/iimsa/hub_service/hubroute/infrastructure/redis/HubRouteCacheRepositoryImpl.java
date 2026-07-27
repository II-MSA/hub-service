package org.iimsa.hub_service.hubroute.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.model.HubRouteEdgeKey;
import org.iimsa.hub_service.hubroute.domain.repository.HubRouteCacheRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // 키 포맷
    private static final String LIVE_KEY = "hub:route:live:%s:%s";

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

    @Override
    public Map<HubRouteEdgeKey, LiveRouteCache> getLiveBulk(Collection<HubRouteEdgeKey> edges) {
        if (edges.isEmpty()) {
            return Map.of();
        }

        List<HubRouteEdgeKey> edgeList = new ArrayList<>(edges);
        List<String> keys = edgeList.stream()
                .map(e -> liveKey(e.fromHubId(), e.toHubId()))
                .toList();

        List<String> values;
        try {
            values = redisTemplate.opsForValue().multiGet(keys);
        } catch (Exception e) {
            log.warn("Redis 벌크 캐시 조회 실패 - size={}, error={}", keys.size(), e.getMessage());
            return Map.of();
        }

        if (values == null) {
            return Map.of();
        }

        Map<HubRouteEdgeKey, LiveRouteCache> result = new HashMap<>();
        for (int i = 0; i < edgeList.size(); i++) {
            String json = values.get(i);
            if (json == null) {
                continue;
            }
            try {
                result.put(edgeList.get(i), objectMapper.readValue(json, LiveRouteCache.class));
            } catch (Exception e) {
                log.warn("Redis 벌크 캐시 파싱 실패 - key={}, error={}", keys.get(i), e.getMessage());
            }
        }

        log.debug("Redis 벌크 live 캐시 조회 완료 - 요청={}, 히트={}", edgeList.size(), result.size());
        return result;
    }

    // ── 내부 유틸 ──────────────────────────────────

    private String liveKey(UUID fromHubId, UUID toHubId) {
        return String.format(LIVE_KEY, fromHubId, toHubId);
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
