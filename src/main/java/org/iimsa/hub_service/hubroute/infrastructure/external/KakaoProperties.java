package org.iimsa.hub_service.hubroute.infrastructure.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 카카오 모빌리티 API 설정
 *
 * <pre>
 * kakao:
 *   mobility:
 *     api-key: ${KAKAO_MOBILITY_API_KEY}
 *     url: https://apis-navi.kakaomobility.com
 * </pre>
 */
@ConfigurationProperties(prefix = "kakao.mobility")
public record KakaoProperties(
        String apiKey,
        String url
) {
}
