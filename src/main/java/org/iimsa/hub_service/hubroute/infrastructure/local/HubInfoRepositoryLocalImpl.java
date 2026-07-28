package org.iimsa.hub_service.hubroute.infrastructure.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.hub_service.hub.application.query.HubQueryService;
import org.iimsa.hub_service.hub.domain.model.HubId;
import org.iimsa.hub_service.hub.domain.query.HubQueryDto;
import org.iimsa.hub_service.hub.presentation.dto.response.GetHubResponseDto;
import org.iimsa.hub_service.hubroute.domain.model.HubInfo;
import org.iimsa.hub_service.hubroute.domain.repository.HubInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

/**
 * HubInfoRepository 인프로세스(직접 호출) 구현체
 *
 * <p>hub 서비스와 hubroute 서비스는 물리적으로 분리된 별도 서버가 아니라 하나의
 * hub-service 프로세스 안에 있는 두 Bounded Context입니다. 이전에는 이 사실과
 * 무관하게 hub/hubroute 사이를 Feign(HTTP, localhost 자기 자신 호출)으로 연결했는데,
 * 이는 같은 JVM 안에서 불필요한 직렬화/역직렬화·네트워크 스택 비용을 지불하는
 * 구조였고 실제로 N+1 성능 문제의 원인이 되기도 했습니다.
 *
 * <p>이 구현체는 {@link HubQueryService} 를 메서드로 직접 호출합니다. 포트
 * 인터페이스({@link HubInfoRepository})는 그대로 유지하므로, 추후 hub 서비스가
 * 물리적으로 독립된 서버로 분리되는 시점에는 이 클래스만 Feign 기반 어댑터로
 * 교체하면 됩니다(도메인/애플리케이션 계층은 변경 불필요).
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class HubInfoRepositoryLocalImpl implements HubInfoRepository {

    private final HubQueryService hubQueryService;

    @Override
    public HubInfo findHub(UUID hubId) {
        GetHubResponseDto.Info info = hubQueryService.getHub(hubId);
        return toHubInfo(info);
    }

    /**
     * hubIds 를 50개씩 청크로 나눠 HubQueryService.searchHubs() 를 호출하는 배치 크기.
     *
     * <p>HubQueryService.validatePageable() 이 페이지 크기를 10/30/50 중 하나로
     * 강제하므로 50이 한 번에 조회 가능한 최댓값입니다. 청크 크기를 페이지 size 와
     * 동일하게 맞추면 요청한 hubIds 개수가 항상 결과 한 페이지 안에 들어옵니다.
     */
    private static final int BATCH_SIZE = 50;

    @Override
    public Map<UUID, HubInfo> findHubsByIds(Set<UUID> hubIds) {
        if (hubIds.isEmpty()) {
            return Map.of();
        }

        List<UUID> idList = new ArrayList<>(hubIds);
        Map<UUID, HubInfo> result = new HashMap<>(hubIds.size() * 2);
        int batchCount = 0;

        for (int from = 0; from < idList.size(); from += BATCH_SIZE) {
            List<UUID> chunk = idList.subList(from, Math.min(from + BATCH_SIZE, idList.size()));
            batchCount++;
            try {
                List<HubId> chunkIds = chunk.stream().map(HubId::of).toList();
                HubQueryDto.Search search = HubQueryDto.Search.builder()
                        .hubIds(chunkIds)
                        .build();

                Page<GetHubResponseDto.Info> page =
                        hubQueryService.searchHubs(search, PageRequest.of(0, BATCH_SIZE));

                for (GetHubResponseDto.Info info : page.getContent()) {
                    result.put(info.getHubId(), toHubInfo(info));
                }
            } catch (Exception e) {
                log.warn("허브 배치 조회 실패 — chunkSize={}, 이유={}", chunk.size(), e.getMessage());
            }
        }

        log.debug("허브 좌표 일괄 조회 완료 — 요청={}, 성공={}, 배치 호출 수={}",
                hubIds.size(), result.size(), batchCount);
        return result;
    }

    private HubInfo toHubInfo(GetHubResponseDto.Info info) {
        return new HubInfo(
                info.getHubId(),
                info.getHubName(),
                info.getAddress(),
                info.getLatitude(),
                info.getLongitude()
        );
    }
}
