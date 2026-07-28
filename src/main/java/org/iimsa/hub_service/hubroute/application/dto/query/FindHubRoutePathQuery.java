package org.iimsa.hub_service.hubroute.application.dto.query;

import java.util.UUID;

public record FindHubRoutePathQuery(
        UUID originHubId,
        UUID destinationHubId,
        Algorithm algorithm
) {
    /**
     * 경로 탐색 알고리즘 선택
     *
     * <ul>
     *   <li>{@link #ASTAR}     — 유클리드 휴리스틱을 활용한 A* (기본값, 탐색 노드 수 최소화)</li>
     *   <li>{@link #DIJKSTRA}  — 휴리스틱 없는 순수 다익스트라 (h = 0, 비교 기준선)</li>
     * </ul>
     */
    public enum Algorithm {
        ASTAR,
        DIJKSTRA
    }

    /** algorithm 미지정 시 A* 로 동작하는 편의 생성자 */
    public FindHubRoutePathQuery(UUID originHubId, UUID destinationHubId) {
        this(originHubId, destinationHubId, Algorithm.ASTAR);
    }
}
