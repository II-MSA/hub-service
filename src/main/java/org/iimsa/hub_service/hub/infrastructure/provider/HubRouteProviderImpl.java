package org.iimsa.hub_service.hub.infrastructure.provider;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hub.domain.model.HubRoute;
import org.iimsa.hub_service.hub.domain.model.HubRoutePath;
import org.iimsa.hub_service.hub.domain.service.HubRouteProvider;
import org.iimsa.hub_service.hub.domain.service.dto.HubRoutePathData;
import org.iimsa.hub_service.hubroute.application.dto.query.FindHubRoutePathQuery;
import org.iimsa.hub_service.hubroute.application.service.HubRouteApplicationService;
import org.springframework.stereotype.Component;

/**
 * HubRouteProvider 인프로세스(직접 호출) 구현체
 *
 * <p>hub 도메인과 hubroute 도메인은 물리적으로 분리된 별도 서버가 아니라 하나의
 * hub-service 프로세스 안에 있는 두 Bounded Context입니다. 이전에는 이 사실과
 * 무관하게 hub → hubroute 호출을 Feign(HTTP, localhost 자기 자신 호출)으로 연결했는데,
 * 같은 JVM 안에서 불필요한 직렬화/역직렬화·네트워크 스택 비용을 지불하는 구조였습니다.
 *
 * <p>이 구현체는 {@link HubRouteApplicationService#findOptimalRoute} 를 메서드로
 * 직접 호출합니다. 포트 인터페이스({@link HubRouteProvider})는 그대로 유지하므로,
 * 추후 hubroute 가 물리적으로 독립된 서버로 분리되는 시점에는 이 클래스만 Feign
 * 기반 어댑터로 교체하면 됩니다(도메인/애플리케이션 계층은 변경 불필요).
 *
 * <p>경로를 찾지 못하면 hubroute 쪽에서 던지는
 * {@link org.ticketing.common.exception.NotFoundException} 이 그대로 전파됩니다
 * (별도의 Feign 예외 변환이 필요 없어짐).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HubRouteProviderImpl implements HubRouteProvider {

    private final HubRouteApplicationService hubRouteApplicationService;

    @Override
    public HubRoutePathData getHubRoute(UUID startHubId, UUID endHubId) {

        // 1. hubroute 애플리케이션 서비스 직접 호출 (프로세스 내부 호출, Feign 아님)
        org.iimsa.hub_service.hubroute.domain.model.HubRoutePath routePath =
                hubRouteApplicationService.findOptimalRoute(new FindHubRoutePathQuery(startHubId, endHubId));

        // 2. hubroute 도메인 Segment -> hub 도메인(HubRoute) 리스트로 변환
        List<HubRoute> routes = routePath.segments().stream()
                .map(segment -> HubRoute.builder()
                        .fromHubId(segment.fromHubId())
                        .fromHubName(segment.fromHubName())
                        .toHubId(segment.toHubId())
                        .toHubName(segment.toHubName())
                        .estimatedDistance(segment.estimatedDistance())
                        .estimatedDuration(segment.estimatedDuration())
                        .build()
                )
                .toList();

        // 3. hub 도메인 리스트를 묶어서 하나의 큰 도메인(HubRoutePath)으로 생성
        HubRoutePath hubRoutePath = HubRoutePath.of(startHubId, endHubId, routes);

        // 4. 도메인(HubRoutePath)을 최종 응답 DTO(HubRoutePathData)로 변환하여 반환
        return HubRoutePathData.from(hubRoutePath);
    }
}
