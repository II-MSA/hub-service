package org.iimsa.hub_service.hubroute.application.dto.query;

import java.util.UUID;

public record ListHubRouteQuery(
        UUID fromHubId, // null 이면 전체 조회
        int page,
        int size
) {
    public ListHubRouteQuery {
        if (page < 0) throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        if (size < 1) throw new IllegalArgumentException("size는 1 이상이어야 합니다.");
    }
}
