package org.iimsa.hub_service.hubroute.domain.service;

import org.iimsa.hub_service.hubroute.domain.model.HubRoute;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 허브 간 최적 경로 계산 도메인 서비스
 *
 * <p>전체 허브 경로(엣지) 그래프를 입력받아 출발 허브 → 도착 허브의
 * 최적 구간 시퀀스를 반환합니다.
 *
 * <p>TODO: 다익스트라 알고리즘 구현
 * <ul>
 *   <li>가중치: estimatedDuration (소요시간 최소화)</li>
 *   <li>estimatedDuration null 시 estimatedDistance * 10 으로 대체</li>
 *   <li>둘 다 null 이면 대형 상수 (최후순위)</li>
 *   <li>경로 없으면 빈 리스트 반환</li>
 * </ul>
 */
@Service
public class OptimalRouteCalculator {

    /**
     * 출발 허브에서 도착 허브까지의 최적 구간 시퀀스 계산
     *
     * @param originHubId      출발 허브 ID
     * @param destinationHubId 도착 허브 ID
     * @param allRoutes        전체 활성 허브 경로 (그래프 엣지)
     * @return 순서대로 정렬된 HubRoute 구간 목록, 경로 없으면 빈 리스트
     */
    public List<HubRoute> calculate(UUID originHubId, UUID destinationHubId, List<HubRoute> allRoutes) {
        // TODO: 다익스트라 알고리즘 구현
        throw new UnsupportedOperationException("최적 경로 알고리즘 미구현");
    }
}
