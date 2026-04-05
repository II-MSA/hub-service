package org.iimsa.hub_service.hubroute.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.application.ExternalRouteTimePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 카카오 모빌리티 API 기반 실시간 소요시간 조회
 *
 * <p>TODO: 카카오 모빌리티 Directions API 연동
 * <ul>
 *   <li>API 키 설정: spring.kakao.mobility.api-key</li>
 *   <li>엔드포인트: POST https://apis-navi.kakaomobility.com/v1/directions</li>
 *   <li>요청: origin(경도,위도) / destination(경도,위도) — 카카오 API는 경도 우선</li>
 *   <li>응답: routes[0].summary.duration(초→분 변환), routes[0].summary.distance(m→km 변환)</li>
 *   <li>참고: https://developers.kakao.com/docs/latest/ko/local/dev-guide</li>
 * </ul>
 */
@Slf4j
@Component
public class KakaoRouteTimeClientImpl implements ExternalRouteTimePort {

    @Override
    public Optional<RouteTimeResult> fetch(double fromLat, double fromLng, double toLat, double toLng) {
        // TODO: 카카오 모빌리티 API 연동
        //   1. RestClient / WebClient 로 Directions API 호출
        //      - origin      : "{fromLng},{fromLat}" (카카오 API는 경도,위도 순)
        //      - destination : "{toLng},{toLat}"
        //   2. 응답 파싱
        //      - duration : routes[0].summary.duration (초) → / 60 → 분 (올림)
        //      - distance : routes[0].summary.distance (m)  → / 1000.0 → km
        //   3. RouteTimeResult 반환
        log.debug("외부 API 미구현 — from=({},{}) to=({},{})", fromLat, fromLng, toLat, toLng);
        return Optional.empty();
    }
}
