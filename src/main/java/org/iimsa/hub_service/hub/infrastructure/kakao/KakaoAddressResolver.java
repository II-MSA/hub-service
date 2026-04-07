package org.iimsa.hub_service.hub.infrastructure.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.iimsa.common.exception.BadRequestException;
import org.iimsa.common.exception.InternalServerException;
import org.iimsa.hub_service.hub.domain.service.AddressResolver;
import org.iimsa.hub_service.hub.domain.service.Coordinates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class KakaoAddressResolver implements AddressResolver {
    @Value("${KAKAO_API_KEY}")
    private String apiKey;

    private final RestClient restClient;

    public KakaoAddressResolver() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .requestFactory(factory)
                .build();
    }

    @Override
    public Coordinates resolve(String address) {
        if (!StringUtils.hasText(address)) {
            throw new BadRequestException("조회할 주소가 비어있습니다.");
        }

        try {
            JsonNode body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", address)
                            .build())
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    // 4xx, 5xx 에러 발생 시 자동으로 예외를 던지도록 설정 가능
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.error("Kakao API Error: {}", response.getStatusCode());
                        throw new InternalServerException("외부 API 호출 실패");
                    })
                    .body(JsonNode.class);

            if (body != null) {
                JsonNode documents = body.get("documents");
                if (documents != null && !documents.isEmpty()) {
                    JsonNode firstDoc = documents.get(0);
                    double lon = firstDoc.get("x").asDouble();
                    double lat = firstDoc.get("y").asDouble();

                    log.debug("Address resolved: {} -> Coordinates: {}, {}", maskAddress(address), lat, lon);
                    return new Coordinates(lat, lon);
                }
            }
        } catch (Exception e) {
            log.error("주소 변환 중 예외 발생: Error: {}", e.getMessage(), e);
        }

        throw new BadRequestException("유효하지 않은 주소입니다: " + address);
    }

    private String maskAddress(String address) {
        if (!StringUtils.hasText(address)) return "";

        String[] parts = address.split(" ");
        if (parts.length <= 2) {
            return "***";
        }

        return parts[0] + " " + parts[1] + " ***";
    }
}

