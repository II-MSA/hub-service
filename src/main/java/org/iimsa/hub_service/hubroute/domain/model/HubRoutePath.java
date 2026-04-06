package org.iimsa.hub_service.hubroute.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 출발 허브 → 도착 허브 간 최적 경로 전체를 표현하는 도메인 값 객체
 *
 * <p>여러 {@link HubRoute} 구간(엣지)을 순서대로 조합한 결과입니다.
 * 예: 서울 → 대전 → 대구 → 부산은 3개의 Segment로 표현됩니다.
 */
public record HubRoutePath(

        UUID originHubId,
        UUID destinationHubId,

        /** 전체 예상 소요시간 합산 (분) */
        int totalDuration,

        /** 전체 예상 거리 합산 (km) */
        double totalDistance,

        /** 순서가 있는 구간 목록 */
        List<Segment> segments

) {
    /**
     * 순서가 정해진 HubRoute 목록으로부터 HubRoutePath 생성
     *
     * @param originHubId      출발 허브 ID
     * @param destinationHubId 도착 허브 ID
     * @param orderedRoutes    알고리즘이 반환한 순서대로 정렬된 HubRoute 목록
     */
    public static HubRoutePath of(UUID originHubId, UUID destinationHubId, List<HubRoute> orderedRoutes) {
        List<Segment> segments = new ArrayList<>();
        int totalDuration = 0;
        double totalDistance = 0.0;

        for (int i = 0; i < orderedRoutes.size(); i++) {
            HubRoute route = orderedRoutes.get(i);
            segments.add(new Segment(
                    i + 1,
                    route.getFromHubId(),
                    route.getFromHubName(),
                    route.getToHubId(),
                    route.getToHubName(),
                    route.getEstimatedDuration(),
                    route.getEstimatedDistance()
            ));
            if (route.getEstimatedDuration() != null) totalDuration += route.getEstimatedDuration();
            if (route.getEstimatedDistance() != null) totalDistance += route.getEstimatedDistance();
        }

        return new HubRoutePath(originHubId, destinationHubId, totalDuration, totalDistance, segments);
    }

    /** 단일 허브 구간 정보 */
    public record Segment(
            int sequence,
            UUID fromHubId,
            String fromHubName,
            UUID toHubId,
            String toHubName,
            Integer estimatedDuration,  // 분
            Double estimatedDistance    // km
    ) {
    }
}
