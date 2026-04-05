package org.iimsa.hub_service.hubroute.domain.service;

import java.util.Optional;

/**
 * 외부 API 실시간 소요시간 조회 클라이언트 인터페이스
 * 구현체: infrastructure/external/KakaoRouteTimeClientImpl (TODO)
 */
public interface ExternalRouteTimeClient {

    /**
     * 두 좌표 간 실시간 소요시간 조회
     *
     * @param fromLat 출발 허브 위도
     * @param fromLng 출발 허브 경도
     * @param toLat   도착 허브 위도
     * @param toLng   도착 허브 경도
     * @return 소요시간(분) + 거리(km), 외부 API 실패 시 empty
     */
    Optional<RouteTimeResult> fetch(double fromLat, double fromLng, double toLat, double toLng);

    record RouteTimeResult(
            Integer duration,  // 분
            Double distance    // km
    ) {
    }
}
