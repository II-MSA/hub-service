package org.iimsa.hub_service.hubroute.infrastructure.feign;

import org.ticketing.common.response.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/**
 * Hub 서비스 Feign 클라이언트
 *
 * <p>URL은 application.yml의 {@code feign.client.hub-service.url} 로 설정합니다.
 * <pre>
 * feign:
 *   client:
 *     hub-service:
 *       url: http://hub-service
 * </pre>
 */
@FeignClient(name = "hub-service", url = "${feign.client.hub-service.url}")
public interface HubFeignClient {

    @GetMapping("/hubs/{hubId}")
    CommonResponse<HubFeignResponse> getHub(@PathVariable UUID hubId);

    /**
     * hubIds 목록으로 허브 정보를 한 번에 조회합니다 (GET /hubs?hubIds=...).
     *
     * <p>Hub 서비스의 {@code HubQueryService.validatePageable()} 이 페이지 크기를
     * 10/30/50 중 하나로 강제하므로, 호출 측에서는 hubIds 를 50개 단위로 청크로
     * 나눠 호출해야 합니다 (청크 크기 == size 이면 항상 단일 페이지로 전체 결과를 받습니다).
     */
    @GetMapping("/hubs")
    CommonResponse<HubPageResponse> searchHubsByIds(
            @RequestParam("hubIds") List<UUID> hubIds,
            @RequestParam("page") int page,
            @RequestParam("size") int size);
}
