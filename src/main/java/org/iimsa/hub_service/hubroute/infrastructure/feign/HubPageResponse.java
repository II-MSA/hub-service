package org.iimsa.hub_service.hubroute.infrastructure.feign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * GET /hubs (hubIds 기반 배치 조회) 응답의 data 필드에 매핑
 *
 * <p>Hub 서비스는 Spring Data {@code Page<Info>} 를 그대로 직렬화해서 내려주는데,
 * content/last 두 필드만 필요하므로 나머지 필드(pageable, sort 등)는 무시합니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HubPageResponse(
        List<HubFeignResponse> content,
        boolean last
) {
}
