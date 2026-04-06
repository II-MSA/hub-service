package org.iimsa.hub_service.hub.infrastructure.provider;

import feign.FeignException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.common.response.CommonResponse;
import org.iimsa.hub_service.hub.domain.model.HubRoute;
import org.iimsa.hub_service.hub.domain.model.HubRoutePath;
import org.iimsa.hub_service.hub.domain.service.HubRouteProvider;
import org.iimsa.hub_service.hub.domain.service.dto.HubRoutePathData;
import org.iimsa.hub_service.hub.infrastructure.client.HubRouteClient;
import org.iimsa.hub_service.hub.infrastructure.client.dto.HubRoutePathResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubRouteProviderImpl implements HubRouteProvider {

    private final HubRouteClient client;

    @Override
    public HubRoutePathData getHubRoute(UUID startHubId, UUID endHubId) {

        try {
            // 1. Feign 호출
            CommonResponse<HubRoutePathResponse> res = client.findOptimalRoute(startHubId, endHubId);

            if (res == null || res.data() == null) {
                log.error("경로 조회 실패: startHubId={}, endHubId={}", startHubId, endHubId);
                throw new RuntimeException("허브 간 경로를 찾을 수 없습니다.");
            }

            HubRoutePathResponse responseData = res.data();

            // 2. 외부 응답 DTO -> 내부 도메인(HubRoute) 리스트로 변환
            List<HubRoute> routes = responseData.segments().stream()
                    .map(segmentDto -> HubRoute.builder()
                            .fromHubId(segmentDto.fromHubId())
                            .fromHubName(segmentDto.fromHubName())
                            .toHubId(segmentDto.toHubId())
                            .toHubName(segmentDto.toHubName())
                            .estimatedDistance(segmentDto.estimatedDistance())
                            .estimatedDuration(segmentDto.estimatedDuration())
                            .build()
                    )
                    .toList();

            // 3. 내부 도메인 리스트를 묶어서 하나의 큰 도메인(HubRoutePath)으로 생성
            HubRoutePath hubRoutePath = HubRoutePath.of(startHubId, endHubId, routes);

            // 4. 도메인(HubRoutePath)을 최종 응답 DTO(HubRoutePathData)로 변환하여 반환
            return HubRoutePathData.from(hubRoutePath);

        } catch (FeignException.NotFound e) {
            log.error("HubRouteClient 호출 중 에러 발생", e);
            throw new RuntimeException("경로 데이터 제공 중 오류가 발생했습니다.", e);
        }
    }
}
