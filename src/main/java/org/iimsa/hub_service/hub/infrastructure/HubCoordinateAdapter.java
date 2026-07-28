package org.iimsa.hub_service.hub.infrastructure;

import lombok.RequiredArgsConstructor;
import org.iimsa.hub_service.hub.domain.repository.HubRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * HubCoordinatePort 구현체 (어댑터)
 *
 * <p>HubRoute application이 Hub의 위도/경도를 조회할 수 있도록
 * Hub 도메인의 {@link HubRepository}를 위임합니다.
 */
@Component
@RequiredArgsConstructor
public class HubCoordinateAdapter {

    private final HubRepository hubRepository;


}
