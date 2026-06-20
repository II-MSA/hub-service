package org.iimsa.hub_service.hub.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 로컬 개발 및 벤치마크용 Security 설정
 *
 * <p>다음 두 가지 목적으로 인증을 우회합니다:
 * <ol>
 *   <li>허브 좌표 조회 (Feign 내부 호출): GET /api/v1/hubs/** → A* 휴리스틱 정상 동작</li>
 *   <li>경로 탐색 API (k6 벤치마크): GET /api/v1/hub-routes/path → 인증 없이 호출 가능</li>
 * </ol>
 *
 * <p>공통 모듈의 SecurityFilterChain보다 높은 우선순위(@Order(1))로 등록합니다.
 */
@Configuration
public class InternalApiSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain internalApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/hubs/**", "/api/v1/hub-routes/path")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
