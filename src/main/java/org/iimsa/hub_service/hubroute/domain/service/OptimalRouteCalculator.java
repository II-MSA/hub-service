package org.iimsa.hub_service.hubroute.domain.service;

import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.model.HubInfo;
import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.iimsa.hub_service.hubroute.domain.model.HubRouteEdgeKey;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 허브 간 최적 경로 계산 도메인 서비스 — A* 알고리즘 (유클리드 휴리스틱)
 *
 * <h3>알고리즘</h3>
 * <p>f(n) = g(n) + h(n)
 * <ul>
 *   <li>g(n): 출발 허브에서 n까지의 실제 누적 비용 (구간별 실시간/기본 소요시간 기반)</li>
 *   <li>h(n): n에서 목적지까지의 평면 유클리드 직선거리 기반 추정 비용</li>
 * </ul>
 *
 * <h3>휴리스틱 설계</h3>
 * <p>실제 엣지 비용(소요시간)은 Redis live 캐시(실시간) 또는 DB 저장값(기본)을 사용하므로,
 * 휴리스틱은 I/O 없이 즉시 계산 가능한 좌표 기반 직선거리를 활용합니다.
 * Haversine(구면 거리) 대신 평면 유클리드 거리를 사용합니다.
 * 한국 국토 규모(최대 ~1,000km)에서 두 공식의 오차는 0.3% 미만이므로
 * 이미 근사값인 휴리스틱에서 Haversine의 추가 복잡도는 의미가 없습니다.
 *
 * <h3>휴리스틱 허용성(Admissibility) 보장</h3>
 * <p>{@value MAX_HUB_SPEED_KM_PER_MIN} km/min({@value MAX_HUB_SPEED_KM_PER_MIN} × 60 = 90 km/h)를
 * 허브 간 이동 가능한 최대 속도로 가정하여 h(n) = distKm / speed 로 분 단위 추정값을 계산합니다.
 * 실제 소요시간은 정차·상하차·교통 등으로 항상 이 값 이상이므로 h(n) ≤ 실제 비용이 성립하고
 * A*는 항상 최적 경로를 반환합니다.
 * 좌표가 없는 노드에서는 h = 0 으로 fallback 하여 다익스트라처럼 동작합니다.
 *
 * <h3>거리 제약</h3>
 * <p>단일 구간 {@value MAX_DIRECT_DISTANCE_KM}km 이상 엣지는 그래프에서 제외합니다.
 *
 * <h3>구간 가중치 — 실시간 우선, DB 정적값 폴백</h3>
 * <p>{@link #warmCache}가 전달받는 {@code liveWeights}(Redis에서 벌크 조회된 구간별 실시간
 * 소요시간)를 우선 사용하고, 값이 없는 구간만 {@link HubRoute}의 {@code estimatedDuration}/
 * {@code estimatedDistance}(DB 정적값)로 폴백합니다. 가중치는 그래프 빌드 시점에 한 번만
 * 계산되어 {@link Edge}에 baked-in 되므로, 탐색 도중에는 순수 in-memory 조회만 발생합니다
 * (탐색 중 구간별 개별 조회는 N+1 패턴을 재현하므로 의도적으로 배제).
 *
 * <h3>인메모리 그래프 캐싱</h3>
 * <p>{@link GraphCache}에 인접 리스트와 허브 좌표를 함께 보관하고,
 * 경로 CRUD 이벤트 또는 실시간 소요시간 갱신 시 {@link #invalidateGraph()}로 무효화합니다.
 * 다음 경로 탐색 시 {@link HubRouteApplicationService}가
 * {@link #needsRebuild()}를 확인한 뒤 {@link #warmCache}를 호출하여 재빌드합니다.
 *
 * <h3>스레드 안전성</h3>
 * <p>읽기 경합은 ReadLock으로 병렬 처리, 빌드·무효화는 WriteLock으로 직렬화합니다.
 */
@Service
public class OptimalRouteCalculator {

    /** 단일 구간 직접 배송 허용 최대 거리 (km). 이 값 이상이면 중간 경유지 필수. */
    static final double MAX_DIRECT_DISTANCE_KM = 200.0;

    /**
     * 허브 간 이동 최대 속도 (km/min = 90 km/h).
     * 보수적으로 설정할수록 h 값이 작아져 허용성이 강하게 보장됩니다.
     * 공격적으로 높이면 탐색 노드는 더 줄지만 최적 경로를 놓칠 위험이 있습니다.
     */
    private static final double MAX_HUB_SPEED_KM_PER_MIN = 1.5; // 90 km/h

    private static final int DISTANCE_TO_DURATION_FACTOR = 10;
    private static final int WEIGHT_UNKNOWN = 999_999;

    // ── 인메모리 그래프 캐시 ──────────────────────────────────────────────────────

    /**
     * 그래프 간선. 원본 {@link HubRoute}와 빌드 시점에 확정된 가중치를 함께 보관합니다.
     * (실시간 캐시 유무를 매 탐색마다 다시 판단하지 않도록 build-time에 한 번만 계산)
     */
    private record Edge(HubRoute route, int weight) {
        UUID toHubId() {
            return route.getToHubId();
        }
    }

    /**
     * 인접 리스트와 허브 좌표를 함께 보관하는 캐시 단위.
     * 두 데이터를 한 레코드로 묶어 무효화 시 원자적으로 처리합니다.
     */
    private record GraphCache(
            Map<UUID, List<Edge>> adjacency,
            Map<UUID, HubInfo> hubCoords
    ) {}

    /** null 이면 캐시 미적재 상태. volatile 로 DCL 가시성 보장. */
    private volatile GraphCache cachedGraph = null;
    private final ReadWriteLock graphLock = new ReentrantReadWriteLock();

    // ── PQ 노드 레코드 ────────────────────────────────────────────────────────────

    /**
     * f = g + h 값을 고정하여 PQ 순서를 정확하게 유지하는 불변 노드.
     * closed set 으로 중복 처리를 방지하므로 g 는 별도 저장이 불필요합니다.
     */
    private record Entry(int f, UUID id) {}

    /**
     * 경로 계산 결과: 최적 경로 구간 목록 + 탐색 노드 수.
     *
     * <p>{@code nodesExplored}는 알고리즘이 closed set 에 추가한 노드 수로,
     * A* 와 Dijkstra 의 탐색 효율을 비교하는 벤치마크 지표로 활용됩니다.
     */
    public record RouteCalculationResult(List<HubRoute> path, int nodesExplored) {}

    // ── 공개 API ─────────────────────────────────────────────────────────────────

    /**
     * 그래프 캐시가 없어 재빌드가 필요한지 확인합니다.
     *
     * <p>Application Service 는 이 메서드로 캐시 상태를 확인한 뒤
     * 필요할 때만 DB·Feign·Redis 를 호출하여 {@link #warmCache}로 재빌드합니다.
     * true 반환 직후 다른 스레드가 먼저 빌드할 수 있으나, {@link #warmCache} 내부
     * DCL 이 이중 빌드를 막습니다.
     */
    public boolean needsRebuild() {
        graphLock.readLock().lock();
        try {
            return cachedGraph == null;
        } finally {
            graphLock.readLock().unlock();
        }
    }

    /**
     * 그래프 캐시를 빌드합니다 (Double-Checked Locking).
     *
     * <p>동시에 여러 스레드가 진입해도 WriteLock 이후 재확인하여
     * 실제 빌드는 한 번만 수행됩니다.
     *
     * @param allRoutes   전체 활성 허브 경로 (200km 필터 미적용 원본)
     * @param hubCoords   hubId → HubInfo 좌표 맵 (A* 휴리스틱용)
     * @param liveWeights (fromHubId, toHubId) → Redis 실시간 소요시간 맵 (벌크 조회 결과,
     *                    미스인 구간은 DB {@code estimatedDuration}/{@code estimatedDistance}로 폴백)
     */
    public void warmCache(List<HubRoute> allRoutes, Map<UUID, HubInfo> hubCoords,
                           Map<HubRouteEdgeKey, LiveRouteCache> liveWeights) {
        graphLock.writeLock().lock();
        try {
            if (cachedGraph == null) {
                List<HubRoute> eligible = allRoutes.stream()
                        .filter(r -> r.getEstimatedDistance() == null
                                || r.getEstimatedDistance() < MAX_DIRECT_DISTANCE_KM)
                        .toList();
                cachedGraph = new GraphCache(
                        buildAdjacency(eligible, liveWeights),
                        Map.copyOf(hubCoords)
                );
            }
        } finally {
            graphLock.writeLock().unlock();
        }
    }

    /**
     * 그래프 캐시를 무효화합니다.
     *
     * <p>허브 경로 CRUD 이후, 또는 Redis 실시간 소요시간이 주기 갱신된 이후 호출해야 합니다.
     * 다음 탐색 요청 시 Application Service 가 DB·Feign·Redis 를 재조회하여 캐시를 재빌드합니다.
     */
    public void invalidateGraph() {
        graphLock.writeLock().lock();
        try {
            cachedGraph = null;
        } finally {
            graphLock.writeLock().unlock();
        }
    }

    /**
     * A* 알고리즘으로 최적 경로를 계산합니다 (유클리드 휴리스틱 적용).
     *
     * <p>{@link #warmCache}가 먼저 호출되어 있어야 합니다.
     * 캐시가 없으면 {@link IllegalStateException}을 던집니다.
     *
     * @param originHubId      출발 허브 ID
     * @param destinationHubId 도착 허브 ID
     * @return 경로 구간 목록과 탐색 노드 수를 담은 {@link RouteCalculationResult}
     * @throws IllegalStateException 캐시가 초기화되지 않은 경우
     */
    public RouteCalculationResult calculate(UUID originHubId, UUID destinationHubId) {
        return search(originHubId, destinationHubId, true);
    }

    /**
     * 다익스트라 알고리즘으로 최적 경로를 계산합니다 (휴리스틱 없음, h = 0).
     *
     * <p>A*의 h = 0 특수 케이스입니다. 탐색 노드 수가 A*보다 많아 느리지만
     * 음수 가중치가 없는 그래프에서 항상 최적 경로를 반환합니다.
     *
     * @param originHubId      출발 허브 ID
     * @param destinationHubId 도착 허브 ID
     * @return 경로 구간 목록과 탐색 노드 수를 담은 {@link RouteCalculationResult}
     * @throws IllegalStateException 캐시가 초기화되지 않은 경우
     */
    public RouteCalculationResult calculateDijkstra(UUID originHubId, UUID destinationHubId) {
        return search(originHubId, destinationHubId, false);
    }

    /**
     * A* / Dijkstra 공통 탐색 로직.
     *
     * @param useHeuristic true = A* (유클리드 휴리스틱), false = Dijkstra (h = 0)
     */
    private RouteCalculationResult search(UUID originHubId, UUID destinationHubId, boolean useHeuristic) {
        if (originHubId.equals(destinationHubId)) {
            return new RouteCalculationResult(Collections.emptyList(), 0);
        }

        GraphCache cache = getCache();
        if (cache == null) {
            throw new IllegalStateException(
                    "그래프 캐시가 초기화되지 않았습니다. warmCache()를 먼저 호출하세요.");
        }

        // g: 출발 허브로부터 각 노드까지의 실제 누적 비용
        Map<UUID, Integer> g = new HashMap<>();
        // prev: 최단 경로 역추적용 엣지
        Map<UUID, Edge> prev = new HashMap<>();
        // closed: 이미 최적 경로가 확정된 노드 (재탐색 방지)
        Set<UUID> closed = new HashSet<>();

        g.put(originHubId, 0);

        int h0 = useHeuristic ? heuristic(originHubId, destinationHubId, cache.hubCoords()) : 0;
        PriorityQueue<Entry> pq = new PriorityQueue<>(Comparator.comparingInt(Entry::f));
        pq.offer(new Entry(h0, originHubId));

        while (!pq.isEmpty()) {
            Entry curr = pq.poll();

            // closed set: 이미 처리된 노드는 스킵 (휴리스틱 허용성 보장 시 항상 최적)
            if (!closed.add(curr.id())) {
                continue;
            }

            // 목적지 도달 시 조기 종료
            if (curr.id().equals(destinationHubId)) {
                break;
            }

            int gCurr = g.getOrDefault(curr.id(), Integer.MAX_VALUE);

            for (Edge edge : cache.adjacency().getOrDefault(curr.id(), Collections.emptyList())) {
                UUID neighbor = edge.toHubId();

                if (closed.contains(neighbor)) {
                    continue; // 확정된 노드는 재탐색 불필요
                }

                int newG = gCurr + edge.weight();
                if (newG < g.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    g.put(neighbor, newG);
                    prev.put(neighbor, edge);
                    int h = useHeuristic
                            ? heuristic(neighbor, destinationHubId, cache.hubCoords())
                            : 0;
                    pq.offer(new Entry(newG + h, neighbor));
                }
            }
        }

        int nodesExplored = closed.size();

        if (!prev.containsKey(destinationHubId)) {
            return new RouteCalculationResult(Collections.emptyList(), nodesExplored);
        }

        return new RouteCalculationResult(
                reconstructPath(originHubId, destinationHubId, prev),
                nodesExplored
        );
    }

    // ── private helpers ──────────────────────────────────────────────────────────

    private GraphCache getCache() {
        graphLock.readLock().lock();
        try {
            return cachedGraph;
        } finally {
            graphLock.readLock().unlock();
        }
    }

    /**
     * fromHubId 기준 인접 리스트 구성. 구간별 가중치를 이 시점에 한 번만 계산해 Edge에 baked-in 합니다.
     */
    private Map<UUID, List<Edge>> buildAdjacency(List<HubRoute> eligibleRoutes,
                                                  Map<HubRouteEdgeKey, LiveRouteCache> liveWeights) {
        Map<UUID, List<Edge>> graph = new HashMap<>();
        for (HubRoute route : eligibleRoutes) {
            LiveRouteCache live = liveWeights.get(
                    HubRouteEdgeKey.of(route.getFromHubId(), route.getToHubId()));
            int weight = resolveWeight(route, live);
            graph.computeIfAbsent(route.getFromHubId(), k -> new ArrayList<>())
                    .add(new Edge(route, weight));
        }
        return Collections.unmodifiableMap(graph);
    }

    /**
     * 평면 유클리드 직선거리 기반 A* 휴리스틱 (분 단위).
     *
     * <p>좌표가 없는 경우 0 반환 → 다익스트라 동작으로 안전하게 fallback.
     */
    private int heuristic(UUID nodeId, UUID destId, Map<UUID, HubInfo> hubCoords) {
        HubInfo node = hubCoords.get(nodeId);
        HubInfo dest = hubCoords.get(destId);
        if (node == null || dest == null || !node.hasCoordinate() || !dest.hasCoordinate()) {
            return 0;
        }
        double distKm = euclideanKm(
                node.latitude(), node.longitude(),
                dest.latitude(), dest.longitude()
        );
        // 분 단위 변환: distKm / MAX_HUB_SPEED_KM_PER_MIN
        // 최대 속도를 보수적으로 잡아 h ≤ 실제 비용 항상 성립
        return (int) (distKm / MAX_HUB_SPEED_KM_PER_MIN);
    }

    /**
     * 평면 유클리드 공식으로 두 지점 간 직선거리(km)를 계산합니다.
     *
     * <p>위도 1° ≈ 111km, 경도 1° ≈ 111km × cos(위도) 를 적용합니다.
     * Haversine 대비 한국 국토 규모에서 오차 0.3% 미만으로, 휴리스틱 용도로 충분합니다.
     */
    private static double euclideanKm(double lat1, double lon1, double lat2, double lon2) {
        final double KM_PER_DEG_LAT = 111.0;
        double avgLat = (lat1 + lat2) / 2.0;
        double kmPerDegLon = KM_PER_DEG_LAT * Math.cos(Math.toRadians(avgLat));

        double dLat = (lat2 - lat1) * KM_PER_DEG_LAT;
        double dLon = (lon2 - lon1) * kmPerDegLon;
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }

    /**
     * 구간 가중치 계산 (그래프 빌드 시점에 한 번만 호출)
     * <ol>
     *   <li>Redis live 캐시의 duration(분) 우선</li>
     *   <li>Redis live 캐시의 distance(km) × 10 대체</li>
     *   <li>DB estimatedDuration(분) 대체</li>
     *   <li>DB estimatedDistance(km) × 10 대체</li>
     *   <li>모두 null → 999_999 (최후순위)</li>
     * </ol>
     */
    private int resolveWeight(HubRoute route, LiveRouteCache live) {
        if (live != null && live.duration() != null) {
            return live.duration();
        }
        if (live != null && live.distance() != null) {
            return (int) Math.ceil(live.distance() * DISTANCE_TO_DURATION_FACTOR);
        }
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
     */
    private List<HubRoute> reconstructPath(UUID originHubId, UUID destinationHubId,
                                            Map<UUID, Edge> prev) {
        LinkedList<HubRoute> path = new LinkedList<>();
        UUID current = destinationHubId;

        while (!current.equals(originHubId)) {
            Edge edge = prev.get(current);
            if (edge == null) {
                return Collections.emptyList();
            }
            path.addFirst(edge.route());
            current = edge.route().getFromHubId();
        }

        return new ArrayList<>(path);
    }
}
