package org.iimsa.hub_service.hubroute.domain.model;

/**
 * 허브 경로 소요시간 값의 출처
 * fallback 우선순위 순서로 정의
 */
public enum RouteTimeSource {

    /** 외부 API 실시간 조회 (카카오/Google) */
    REALTIME,

    /** p_hub_route_history 테이블 기반 과거 평균값 */
    DB_AVERAGE,

    /** 직선 거리 기반 기본 추정값 */
    BASE_DURATION,

    /** 직전 배차 스냅샷 값 */
    PREVIOUS_SNAPSHOT
}
