package org.iimsa.hub_service.hubroute.infrastructure.lock;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * Redis 기반 스케줄러 분산 락
 *
 * <p>hub-service가 여러 인스턴스로 스케일 아웃되면 {@code @Scheduled} 메서드는 인스턴스마다
 * 독립적으로 실행됩니다. 이 락이 없으면 예를 들어 10분 주기 RouteTimeRefreshScheduler가
 * 인스턴스 수만큼 중복 실행되어 카카오 API를 중복 호출하고(외부 API 쿼터 낭비), 여러
 * 인스턴스가 각자 다른 타이밍에 자신의 인메모리 그래프 캐시를 무효화해 인스턴스 간
 * 잠깐씩 다른 경로 결과를 줄 수 있습니다.
 *
 * <p>{@code SET key value NX EX ttl} 로 락을 선점하고, 해제 시에는 Lua 스크립트로
 * "내가 세팅한 토큰이 맞는지" 확인 후 삭제합니다(compare-and-delete). TTL이 만료돼
 * 이미 다른 인스턴스가 새로 잡은 락을, 뒤늦게 끝난 이전 인스턴스가 실수로 지우는 것을
 * 막기 위함입니다.
 *
 * <p>Redis 장애로 락 자체를 획득할 수 없는 경우의 처리(그대로 실행할지, 스킵할지)는
 * 이 클래스가 결정하지 않고 호출부에 맡깁니다 — 작업의 멱등성/중요도에 따라 정책이
 * 달라질 수 있기 때문입니다(예: RouteTimeRefreshScheduler는 fail-open 정책을 사용).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerDistributedLock {

    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY_PREFIX = "scheduler:lock:";

    private static final RedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "  return redis.call('del', KEYS[1]) "
                    + "else "
                    + "  return 0 "
                    + "end",
            Long.class
    );

    /**
     * taskName 에 대한 락을 시도합니다.
     *
     * @param taskName 스케줄러 작업 식별자 (예: "route-time-refresh")
     * @param ttl      락 유지 시간 — 스케줄 주기보다 짧게 잡아, 인스턴스 비정상 종료로
     *                 unlock 이 호출되지 못해도 다음 스케줄 이전에 자동 해제되도록 함
     * @return 락 획득에 성공하면 해제 시 사용할 토큰, 이미 다른 인스턴스가 보유 중이면 empty
     * @throws org.springframework.data.redis.RedisConnectionFailureException 등 Redis 자체가 응답하지 않는 경우
     *         예외를 그대로 던집니다. "다른 인스턴스가 락을 쥐고 있어서 실패"와 "Redis 장애로 판단 불가"를
     *         호출부에서 구분해 서로 다르게 대응할 수 있도록 하기 위함입니다.
     */
    public Optional<String> tryLock(String taskName, Duration ttl) {
        String key = LOCK_KEY_PREFIX + taskName;
        String token = UUID.randomUUID().toString();

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        if (Boolean.TRUE.equals(acquired)) {
            return Optional.of(token);
        }
        return Optional.empty();
    }

    /**
     * 획득했던 락을 해제합니다. 내가 세팅한 토큰일 때만 삭제됩니다(compare-and-delete).
     * 해제 자체가 실패해도 TTL 로 자동 해제되므로 예외를 던지지 않고 로그만 남깁니다.
     */
    public void unlock(String taskName, String token) {
        String key = LOCK_KEY_PREFIX + taskName;
        try {
            redisTemplate.execute(RELEASE_SCRIPT, List.of(key), token);
        } catch (Exception e) {
            log.warn("[SCHEDULER_LOCK] 락 해제 실패 — task={}, error={} (TTL 만료로 자동 해제될 예정)",
                    taskName, e.getMessage());
        }
    }
}
