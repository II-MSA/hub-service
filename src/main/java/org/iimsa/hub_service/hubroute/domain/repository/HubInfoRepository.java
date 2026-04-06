package org.iimsa.hub_service.hubroute.domain.repository;

import org.iimsa.hub_service.hubroute.domain.model.HubInfo;

import java.util.UUID;

/**
 * HubRoute 도메인이 Hub 정보를 조회하기 위한 도메인 리포지토리
 *
 * <p>허브 경로 생성 시 허브명 조회, 실시간 소요시간 계산 시 좌표 조회에 사용합니다.
 * 구현체: infrastructure/feign/HubInfoRepositoryImpl (Feign)
 */
public interface HubInfoRepository {

    /**
     * 허브 ID로 허브 정보 조회
     *
     * @throws org.iimsa.common.exception.NotFoundException 허브가 존재하지 않으면
     */
    HubInfo findHub(UUID hubId);
}
