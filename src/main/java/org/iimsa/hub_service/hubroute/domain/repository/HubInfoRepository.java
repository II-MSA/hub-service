package org.iimsa.hub_service.hubroute.domain.repository;

import org.iimsa.hub_service.hubroute.domain.model.HubInfo;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * HubRoute 도메인이 Hub 정보를 조회하기 위한 도메인 리포지토리
 *
 * <p>허브 경로 생성 시 허브명 조회, 실시간 소요시간 계산 시 좌표 조회에 사용합니다.
 * 구현체: infrastructure/local/HubInfoRepositoryLocalImpl (같은 프로세스 내 직접 호출).
 * hub 서비스가 물리적으로 분리되면 Feign 기반 어댑터로 교체될 수 있습니다.
 */
public interface HubInfoRepository {

    /**
     * 허브 ID로 허브 정보 조회
     *
     * @throws org.ticketing.common.exception.NotFoundException 허브가 존재하지 않으면
     */
    HubInfo findHub(UUID hubId);

    /**
     * 허브 ID 집합으로 허브 정보를 일괄 조회합니다.
     *
     * <p>A* 휴리스틱 계산을 위한 좌표 일괄 로드에 사용됩니다.
     * 개별 조회에 실패한 허브는 결과 맵에서 제외되며, 해당 노드에서의 휴리스틱은
     * 0으로 fallback 되어 다익스트라처럼 동작합니다.
     *
     * @param hubIds 조회할 허브 ID 집합
     * @return hubId → HubInfo 맵 (조회 성공한 것만 포함)
     */
    Map<UUID, HubInfo> findHubsByIds(Set<UUID> hubIds);
}
