package org.iimsa.hub_service.hubroute.domain.service;

import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.springframework.stereotype.Service;

import java.util.*;

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
 */
@Service
public class OptimalRouteCalculator {

    /** 단일 구간 직접 배송 허용 최대 거리 (km). 이 값 이상이면 중간 경유지 필수. */
    static final double MAX_DIRECT_DISTANCE_KM = 200.0;

    private static final int DISTANCE_TO_DURATION_FACTOR = 10;
    private static final int WEIGHT_UNKNOWN = 999_999;

    /**
     * 출발 허브에서 도착 허브까지의 최적 구간 시퀀스 계산
     *
     * @param originHubId      출발 허브 ID
     * @param destinationHubId 도착 허브 ID
     * @param allRoutes        전체 활성 허브 경로 (그래프 엣지)
     * @return 순서대로 정렬된 HubRoute 구간 목록, 경로 없으면 빈 리스트
     */
    public List<HubRoute> calculate(UUID originHubId, UUID destinationHubId, List<HubRoute> allRoutes) {
        if (originHubId.equals(destinationHubId)) {
            return Collections.emptyList();
        }

        // ── 정책 필터: 200km 이상 구간은 직접 배송 불가 → 그래프에서 제외
        // estimatedDistance가 null인 경우 거리 불명이므로 일단 포함
        List<HubRoute> eligibleRoutes = allRoutes.stream()
                .filter(r -> r.getEstimatedDistance() == null
                        || r.getEstimatedDistance() < MAX_DIRECT_DISTANCE_KM)
                .toList();

        // 인접 리스트 그래프 구성: fromHubId → 출발 가능한 HubRoute 목록
        Map<UUID, List<HubRoute>> graph = buildGraph(eligibleRoutes);

        // dist: 출발 허브로부터 각 노드까지의 현재 최단 비용
        Map<UUID, Integer> dist = new HashMap<>();
        // prev: 최단 경로 역추적용 — 노드에 도달하기 위해 사용된 엣지(HubRoute)
        Map<UUID, HubRoute> prev = new HashMap<>();

        dist.put(originHubId, 0);

        // 우선순위 큐: (비용, hubId) — 비용 오름차순
        PriorityQueue<UUID> pq = new PriorityQueue<>(
                Comparator.comparingInt(id -> dist.getOrDefault(id, Integer.MAX_VALUE))
        );
        pq.add(originHubId);

        while (!pq.isEmpty()) {
            UUID current = pq.poll();

            // 도착 허브 도달 시 조기 종료
            if (current.equals(destinationHubId)) {
                break;
            }

            int currentCost = dist.getOrDefault(current, Integer.MAX_VALUE);

            // 이미 더 짧은 경로로 처리된 노드는 스킵 (PQ 중복 삽입 대응)
            // pq 안에 동일 노드가 여러 번 들어 있을 수 있으므로 현재 dist와 비교
            for (HubRoute edge : graph.getOrDefault(current, Collections.emptyList())) {
                UUID neighbor = edge.getToHubId();
                int newCost = currentCost + weight(edge);

                if (newCost < dist.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    dist.put(neighbor, newCost);
                    prev.put(neighbor, edge);
                    pq.add(neighbor); // 이미 있어도 재삽입 (lazy deletion 방식)
                }
            }
        }

        // 도착 허브에 도달하지 못한 경우
        if (!prev.containsKey(destinationHubId)) {
            return Collections.emptyList();
        }

        return reconstructPath(originHubId, destinationHubId, prev);
    }

    // ──────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────

    /** fromHubId 기준 인접 리스트 구성 */
    private Map<UUID, List<HubRoute>> buildGraph(List<HubRoute> allRoutes) {
        Map<UUID, List<HubRoute>> graph = new HashMap<>();
        for (HubRoute route : allRoutes) {
            graph.computeIfAbsent(route.getFromHubId(), k -> new ArrayList<>()).add(route);
        }
        return graph;
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
                // 역추적 도중 끊기면 경로 없음으로 처리
                return Collections.emptyList();
            }
            path.addFirst(edge);
            current = edge.getFromHubId();
        }

        return new ArrayList<>(path);
    }
}
