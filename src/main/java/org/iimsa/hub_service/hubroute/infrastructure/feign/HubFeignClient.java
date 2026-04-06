package org.iimsa.hub_service.hubroute.infrastructure.feign;

import org.iimsa.common.response.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping("/api/v1/hubs/{hubId}")
    CommonResponse<HubFeignResponse> getHub(@PathVariable UUID hubId);
}
