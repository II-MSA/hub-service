package org.iimsa.hub_service.hubroute.application;

import java.util.Optional;
import java.util.UUID;

/**
 * HubRoute application이 Hub의 위도/경도를 조회하기 위한 아웃바운드 포트
 *
 * <p>HubRoute application layer가 Hub 도메인 내부에 직접 의존하지 않도록 추상화합니다.
 * 구현체: hub/infrastructure/HubCoordinateAdapter
 */
public interface HubCoordinatePort {

    /**
     * 허브 ID로 위도/경도 조회
     *
     * @param hubId 허브 ID
     * @return 허브가 존재하고 좌표가 등록되어 있으면 HubCoordinate, 없으면 empty
     */
    Optional<HubCoordinate> findCoordinate(UUID hubId);

    record HubCoordinate(
            Double latitude,
            Double longitude
    ) {
    }
}
