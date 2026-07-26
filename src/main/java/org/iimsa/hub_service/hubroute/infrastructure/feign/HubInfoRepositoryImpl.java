package org.iimsa.hub_service.hubroute.infrastructure.feign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ticketing.common.exception.NotFoundException;
import org.iimsa.hub_service.hubroute.domain.model.HubInfo;
import org.iimsa.hub_service.hubroute.domain.repository.HubInfoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * HubInfoRepository Feign 구현체
 *
 * <p>Hub 서비스 REST API를 Feign으로 호출하여 허브 정보를 제공합니다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class HubInfoRepositoryImpl implements HubInfoRepository {

    private final HubFeignClient hubFeignClient;

    @Override
    public HubInfo findHub(UUID hubId) {
        var response = hubFeignClient.getHub(hubId);

        if (response == null || response.data() == null) {
            throw new NotFoundException("허브를 찾을 수 없습니다. hubId=" + hubId);
        }

        HubFeignResponse data = response.data();
        return new HubInfo(
                data.hubId(),
                data.hubName(),
                data.address(),
                data.latitude(),
                data.longitude()
        );
    }

    /**
     * hubIds 를 50개씩 청크로 나눠 GET /hubs?hubIds=... 를 호출하는 배치 크기.
     *
     * <p>Hub 서비스의 HubQueryService.validatePageable() 이 페이지 크기를
     * 10/30/50 중 하나로 강제하므로 50이 한 번에 요청 가능한 최댓값입니다.
     * 청크 크기를 페이지 size 와 동일하게 맞추면 요청한 hubIds 개수가 항상
     * 결과 한 페이지 안에 들어오므로 페이지네이션 루프가 필요 없습니다.
     */
    private static final int BATCH_SIZE = 50;

    /**
     * 허브 ID 집합에 대한 허브 정보를 일괄 조회합니다.
     *
     * <p>그래프 캐시 재빌드 시 A* 휴리스틱용 좌표를 한 번에 로드하기 위해 사용됩니다.
     * 캐시 미스(CRUD 이후 첫 경로 탐색)에서만 호출되므로 다소 비용이 높아도 허용됩니다.
     *
     * <p>이전에는 hubId 하나당 Feign 호출을 1번씩 보내는 N+1 패턴이었습니다(500개 허브 →
     * 500회 순차 호출). 지금은 hubIds 를 {@value #BATCH_SIZE}개씩 청크로 나눠
     * GET /hubs?hubIds=... 를 호출하는 방식으로 바꿔, 500개 허브 기준 호출 수를
     * 10회로 줄였습니다.
     *
     * <p>청크 단위 호출이 실패하면 해당 청크의 허브들은 결과에서 제외됩니다.
     * OptimalRouteCalculator 는 좌표가 없는 노드에 대해 h=0 으로 fallback 합니다.
     */
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
                var response = hubFeignClient.searchHubsByIds(chunk, 0, BATCH_SIZE);
                if (response == null || response.data() == null || response.data().content() == null) {
                    log.warn("허브 배치 조회 응답이 비어있음 — chunkSize={}", chunk.size());
                    continue;
                }
                for (HubFeignResponse data : response.data().content()) {
                    result.put(data.hubId(), new HubInfo(
                            data.hubId(),
                            data.hubName(),
                            data.address(),
                            data.latitude(),
                            data.longitude()
                    ));
                }
            } catch (Exception e) {
                log.warn("허브 배치 조회 실패 — chunkSize={}, 이유={}", chunk.size(), e.getMessage());
            }
        }

        log.debug("허브 좌표 일괄 조회 완료 — 요청={}, 성공={}, 배치 호출 수={}",
                hubIds.size(), result.size(), batchCount);
        return result;
    }
}
