package org.iimsa.hub_service.hubroute.domain.service;

import java.util.Optional;
import java.util.UUID;

/**
 * HubRoute 도메인이 Hub의 위도/경도를 조회하기 위한 포트 인터페이스
 *
 * <p>HubRoute 도메인이 Hub 도메인에 직접 의존하지 않도록 추상화합니다.
 * 구현체: hub/infrastructure/HubCoordinateAdapter
 */
public interface HubCoordinatePort {

    /**
     * 허브 ID로 위도/경도 조회
     *
     * @param hubId 허브 ID
     * @return 허브가 존재하고 좌표가 있으면 HubCoordinate, 없으면 empty
     */
    Optional<HubCoordinate> findCoordinate(UUID hubId);

    record HubCoordinate(
            Double latitude,
            Double longitude
    ) {
    }
}
