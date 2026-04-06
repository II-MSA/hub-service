package org.iimsa.hub_service.hubroute.domain.model;

import java.util.UUID;

/**
 * HubRoute 도메인에서 필요로 하는 Hub 정보 값 객체
 *
 * <p>Hub 도메인의 엔티티에 직접 의존하지 않고,
 * HubRoute 도메인이 필요한 필드만 추출한 읽기 전용 값 객체입니다.
 */
public record HubInfo(
        UUID id,
        String name,
        String address,
        Double latitude,
        Double longitude
) {
    public boolean hasCoordinate() {
        return latitude != null && longitude != null;
    }
}
