package org.iimsa.hub_service.hubroute.domain.service;

import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 허브 간 최적 경로 계산 도메인 서비스 — 다익스트라 알고리즘
 *
 * <p>전체 허브 경로(엣지) 그래프를 입력받아 출발 허브 → 도착 허브의
 * 최적 구간 시퀀스를 반환합니다.
 *
 * <h3>거리 제약 (P2P + Hub-to-Hub Relay 정책)</h3>
 * <ul>
 *   <li>단일 구간 거리가 {@value MAX_DIRECT_DISTANCE_KM}km 미만인 엣지만 그래프에 포함</li>
 *   <li>200km 이상인 구간은 직접 배송 불가 → 그래프에서 제외하여 중간 경유지 경로만 선택되도록 강제</li>
 *   <li>거리 정보가 없는(null) 엣지는 제약 판단 불가이므로 그래프에 포함</li>
 * </ul>
 *
 * <h3>가중치 정책</h3>
 * <ol>
 *   <li>{@code estimatedDuration} (분) — 최우선</li>
 *   <li>{@code estimatedDistance * 10} — duration null 시 대체</li>
 *   <li>{@code 999_999} — 둘 다 null 이면 최후순위</li>
 * </ol>
 *
 * <h3>그래프 캐싱</h3>
 * <p>경로 CRUD 가 없는 한 그래프 구조는 변하지 않으므로, 인접 리스트를 인메모리에
 * 캐싱하여 매 요청마다 발생하는 DB allRoutes → buildGraph 비용을 제거합니다.
 * CRUD 이벤트가 발생하면 {@link #invalidateGraph()} 를 호출해 캐시를 무효화합니다.
 *
 * <p>동시성: Double-Checked Locking + {@link ReadWriteLock} 으로 안전하게 보호합니다.
 * 읽기 경합은 ReadLock 으로 병렬 처리하고, 빌드/무효화에만 WriteLock 을 사용합니다.
 */
@Service
public class OptimalRouteCalculator {

    /** 단일 구간 직접 배송 허용 최대 거리 (km). 이 값 이상이면 중간 경유지 필수. */
    static final double MAX_DIRECT_DISTANCE_KM = 200.0;

    private static final int DISTANCE_TO_DURATION_FACTOR = 10;
    private static final int WEIGHT_UNKNOWN = 999_999;

    // ── 인메모리 그래프 캐시 ─────────────────────────────────────────────────────
    /** null 이면 캐시 미적재 상태. volatile 로 DCL 가시성 보장. */
    private volatile Map<UUID, List<HubRoute>> cachedGraph = null;
    private final ReadWriteLock graphLock = new ReentrantReadWriteLock();

    // ── 내부 PQ 노드 레코드 ───────────────────────────────────────────────────────
    /**
     * PriorityQueue 삽입 시점의 비용을 고정 보관하는 불변 노드.
     *
     * <p>Java {@link PriorityQueue} 는 삽입 이후 힙을 재정렬하지 않기 때문에,
     * dist 맵을 직접 comparator 로 사용하면 이미 삽입된 항목의 순위가
     * 갱신되지 않아 잘못된 순서로 poll 될 수 있습니다.
     * 비용을 레코드에 고정함으로써 heap ordering 을 항상 정확하게 유지합니다.
     */
    private record Entry(int cost, UUID id) {}

    // ── 공개 API ─────────────────────────────────────────────────────────────────

    /**
     * 출발 허브에서 도착 허브까지의 최적 구간 시퀀스 계산
     *
     * @param originHubId      출발 허브 ID
     * @param destinationHubId 도착 허브 ID
     * @param allRoutes        전체 활성 허브 경로 (그래프 엣지, 캐시 미적재 시 빌드에 사용)
     * @return 순서대로 정렬된 HubRoute 구간 목록, 경로 없으면 빈 리스트
     */
    public List<HubRoute> calculate(UUID originHubId, UUID destinationHubId, List<HubRoute> allRoutes) {
        if (originHubId.equals(destinationHubId)) {
            return Collections.emptyList();
        }

        Map<UUID, List<HubRoute>> graph = getOrBuildGraph(allRoutes);

        // dist: 출발 허브로부터 각 노드까지의 현재 최단 비용
        Map<UUID, Integer> dist = new HashMap<>();
        // prev: 최단 경로 역추적용 — 노드에 도달하기 위해 사용된 엣지(HubRoute)
        Map<UUID, HubRoute> prev = new HashMap<>();

        dist.put(originHubId, 0);

        // PQ: 삽입 시점 비용을 Entry 에 고정 → heap ordering 항상 정확
        PriorityQueue<Entry> pq = new PriorityQueue<>(Comparator.comparingInt(Entry::cost));
        pq.offer(new Entry(0, originHubId));

        while (!pq.isEmpty()) {
            Entry curr = pq.poll();

            // lazy deletion: 이미 더 짧은 경로로 처리된 오래된 항목은 스킵
            if (curr.cost() > dist.getOrDefault(curr.id(), Integer.MAX_VALUE)) {
                continue;
            }

            // 도착 허브 도달 시 조기 종료
            if (curr.id().equals(destinationHubId)) {
                break;
            }

            for (HubRoute edge : graph.getOrDefault(curr.id(), Collections.emptyList())) {
                UUID neighbor = edge.getToHubId();
                int newCost = curr.cost() + weight(edge);

                if (newCost < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    dist.put(neighbor, newCost);
                    prev.put(neighbor, edge);
                    pq.offer(new Entry(newCost, neighbor)); // 비용 고정하여 재삽입
                }
            }
        }

        if (!prev.containsKey(destinationHubId)) {
            return Collections.emptyList();
        }

        return reconstructPath(originHubId, destinationHubId, prev);
    }

    /**
     * 그래프 캐시를 무효화합니다.
     *
     * <p>허브 경로 CRUD 이후 반드시 호출해야 합니다. 다음 {@link #calculate} 호출 시
     * 새로운 allRoutes 로 그래프가 재빌드됩니다.
     */
    public void invalidateGraph() {
        graphLock.writeLock().lock();
        try {
            cachedGraph = null;
        } finally {
            graphLock.writeLock().unlock();
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────────

    /**
     * Double-Checked Locking 으로 캐시된 그래프를 반환하거나, 없으면 빌드하여 캐싱합니다.
     *
     * <p>읽기 경합: ReadLock — 여러 요청이 동시에 캐시 히트 시 병렬 처리됩니다.
     * 빌드 경합: WriteLock + DCL — 캐시 미스 시 한 번만 빌드되도록 보장합니다.
     */
    private Map<UUID, List<HubRoute>> getOrBuildGraph(List<HubRoute> allRoutes) {
        // 1차 검사: ReadLock 으로 캐시 히트 여부 확인 (대부분의 경로)
        graphLock.readLock().lock();
        try {
            if (cachedGraph != null) {
                return cachedGraph;
            }
        } finally {
            graphLock.readLock().unlock();
        }

        // 2차 검사: WriteLock 획득 후 재확인 (동시 빌드 방지)
        graphLock.writeLock().lock();
        try {
            if (cachedGraph == null) {
                // 정책 필터: 200km 이상 구간 제외 (거리 null 은 포함)
                List<HubRoute> eligibleRoutes = allRoutes.stream()
                        .filter(r -> r.getEstimatedDistance() == null
                                || r.getEstimatedDistance() < MAX_DIRECT_DISTANCE_KM)
                        .toList();
                cachedGraph = buildGraph(eligibleRoutes);
            }
            return cachedGraph;
        } finally {
            graphLock.writeLock().unlock();
        }
    }

    /** fromHubId 기준 인접 리스트 구성 */
    private Map<UUID, List<HubRoute>> buildGraph(List<HubRoute> eligibleRoutes) {
        Map<UUID, List<HubRoute>> graph = new HashMap<>();
        for (HubRoute route : eligibleRoutes) {
            graph.computeIfAbsent(route.getFromHubId(), k -> new ArrayList<>()).add(route);
        }
        return Collections.unmodifiableMap(graph);
    }

    /**
     * 엣지 가중치 계산
     * <ol>
     *   <li>estimatedDuration(분) 우선</li>
     *   <li>estimatedDistance(km) × 10 대체</li>
     *   <li>둘 다 null → 999_999 (최후순위)</li>
     * </ol>
     */
    private int weight(HubRoute route) {
        if (route.getEstimatedDuration() != null) {
            return route.getEstimatedDuration();
        }
        if (route.getEstimatedDistance() != null) {
            return (int) Math.ceil(route.getEstimatedDistance() * DISTANCE_TO_DURATION_FACTOR);
        }
        return WEIGHT_UNKNOWN;
    }

    /**
     * prev 맵을 역추적하여 출발→도착 순서의 HubRoute 리스트 반환
     *
     * @return 순서가 보장된 HubRoute 구간 리스트 (출발 → 도착 방향)
     */
    private List<HubRoute> reconstructPath(UUID originHubId, UUID destinationHubId,
                                            Map<UUID, HubRoute> prev) {
        LinkedList<HubRoute> path = new LinkedList<>();
        UUID current = destinationHubId;

        while (!current.equals(originHubId)) {
            HubRoute edge = prev.get(current);
            if (edge == null) {
                return Collections.emptyList();
            }
            path.addFirst(edge);
            current = edge.getFromHubId();
        }

        return new ArrayList<>(path);
    }
}
