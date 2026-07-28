package org.iimsa.hub_service.hubroute.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hubroute.domain.service.ExternalRouteTimeClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * 카카오 모빌리티 Directions API 기반 실시간 소요시간 조회
 *
 * <p>API 문서: https://developers.kakao.com/docs/latest/ko/local/dev-guide
 * <p>엔드포인트: GET https://apis-navi.kakaomobility.com/v1/directions
 * <p>좌표 입력 순서: 경도(longitude), 위도(latitude) — 카카오 API 표준
 */
@Slf4j
@Component
public class KakaoRouteTimeClientImpl implements ExternalRouteTimeClient {

    private static final String DIRECTIONS_PATH = "/v1/directions";
    private static final int    RESULT_CODE_OK  = 0;

    private final RestClient restClient;

    public KakaoRouteTimeClientImpl(KakaoProperties kakaoProperties) {
        this.restClient = RestClient.builder()
                .baseUrl(kakaoProperties.url())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoProperties.apiKey())
                .build();
    }

    @Override
    public Optional<RouteTimeResult> fetch(double fromLat, double fromLng, double toLat, double toLng) {
        try {
            KakaoDirectionsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(DIRECTIONS_PATH)
                            .queryParam("origin",      formatCoord(fromLng, fromLat))  // 경도,위도
                            .queryParam("destination", formatCoord(toLng, toLat))       // 경도,위도
                            .queryParam("priority", "TIME")
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.warn("[KAKAO API] HTTP 오류 status={} from=({},{}) to=({},{})",
                                res.getStatusCode(), fromLat, fromLng, toLat, toLng);
                    })
                    .body(KakaoDirectionsResponse.class);

            return parseResult(response, fromLat, fromLng, toLat, toLng);

        } catch (Exception e) {
            log.warn("[KAKAO API] 호출 실패 from=({},{}) to=({},{}) error={}",
                    fromLat, fromLng, toLat, toLng, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<RouteTimeResult> parseResult(
            KakaoDirectionsResponse response,
            double fromLat, double fromLng, double toLat, double toLng
    ) {
        if (response == null || response.routes() == null || response.routes().isEmpty()) {
            log.warn("[KAKAO API] 응답 없음 from=({},{}) to=({},{})", fromLat, fromLng, toLat, toLng);
            return Optional.empty();
        }

        KakaoRoute route = response.routes().get(0);

        if (route.resultCode() != RESULT_CODE_OK) {
            log.warn("[KAKAO API] 경로 탐색 실패 resultCode={} resultMsg={} from=({},{}) to=({},{})",
                    route.resultCode(), route.resultMsg(), fromLat, fromLng, toLat, toLng);
            return Optional.empty();
        }

        if (route.summary() == null) {
            log.warn("[KAKAO API] summary 없음 from=({},{}) to=({},{})", fromLat, fromLng, toLat, toLng);
            return Optional.empty();
        }

        int    durationMin = (int) Math.ceil(route.summary().duration() / 60.0); // 초 → 분 (올림)
        double distanceKm  = route.summary().distance() / 1000.0;                // m → km

        log.debug("[KAKAO API] 성공 from=({},{}) to=({},{}) duration={}분 distance={}km",
                fromLat, fromLng, toLat, toLng, durationMin, distanceKm);

        return Optional.of(new RouteTimeResult(durationMin, distanceKm));
    }

    /** 카카오 API 좌표 형식: "경도,위도" */
    private String formatCoord(double lng, double lat) {
        return lng + "," + lat;
    }

    // ── 카카오 API 응답 DTO ────────────────────────────────────

    private record KakaoDirectionsResponse(
            List<KakaoRoute> routes
    ) {
    }

    private record KakaoRoute(
            int resultCode,
            String resultMsg,
            KakaoSummary summary
    ) {
    }

    private record KakaoSummary(
            int duration,   // 소요시간 (초)
            int distance    // 거리 (m)
    ) {
    }
}
